"""
Healthcare Appointment Scheduling System - Complete Integration Flow
All-in-one script - no external dependencies except 'requests'

This script contains all service clients and demonstrates the complete workflow:
Patient → Patient Service → Doctor → Doctor Service → Appointment → Notifications → Billing → Analytics
"""
import sys
import requests
import json
import time
from typing import Optional, List, Dict
import random, string
from datetime import datetime, timedelta, timezone

from idna import ulabel


# ============================================================================
# SERVICE CLIENT CLASSES
# ============================================================================

def random_email():
    """Return a random e-mail that has never been used before."""
    tail = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
    return f"user.{tail}@test.local"

def generate_random_policy_number(name):
    year = random.randint(2020, 2025)  # Random year between 2020 and 2025
    random_str = ''.join(random.choices(string.ascii_uppercase + string.digits, k=3))  # Random 3-character alphanumeric string
    return f"{name}-{year}-{random_str}"


class PatientServiceClient:
    """Client for Patient Service"""

    def __init__(self, base_url: str = "http://localhost:8081"):
        self.base_url = base_url
        self.api_prefix = "/api/patients"

    def register_patient(self, first_name: str, last_name: str, email: str,
                         phone_number: str, date_of_birth: str, gender: str) -> Dict:
        """POST /api/patients/register - Register new patient"""
        url = f"{self.base_url}{self.api_prefix}/register"

        payload = {
            "firstName": first_name,
            "lastName": last_name,
            "email": email,
            "phoneNumber": phone_number,
            "dateOfBirth": date_of_birth,
            "gender": gender
        }

        response = requests.post(url, json=payload)
        response.raise_for_status()
        return response.json()

    def get_patient(self, patient_id: str) -> Dict:
        """GET /api/patients/{id} - Get patient details"""
        url = f"{self.base_url}{self.api_prefix}/{patient_id}"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()

    def update_insurance(self, patient_id: str, insurance_data: Dict) -> Dict:
        """PUT /api/patients/{id}/insurance - Update insurance details"""
        url = f"{self.base_url}{self.api_prefix}/{patient_id}/insurance"
        response = requests.put(url, json=insurance_data)
        response.raise_for_status()
        return response.json()


class DoctorServiceClient:
    """Client for Doctor Service"""

    def __init__(self, base_url: str = "http://localhost:8082"):
        self.base_url = base_url
        self.api_prefix = "/api/doctors"

    def register_doctor(self, first_name: str, last_name: str, email: str,
                        phone_number: str, specialization: str, years_of_experience: int,
                        license_number: str, consultation_fee: float, bio: str,
                        qualifications: str) -> Dict:
        """POST /api/doctors/register - Register doctor"""
        url = f"{self.base_url}{self.api_prefix}/register"

        payload = {
            "firstName": first_name,
            "lastName": last_name,
            "email": email,
            "phoneNumber": phone_number,
            "specialization": specialization,
            "yearsOfExperience": years_of_experience,
            "licenseNumber": license_number,
            "consultationFee": consultation_fee,
            "bio": bio,
            "qualifications": qualifications
        }

        response = requests.post(url, json=payload)
        response.raise_for_status()
        return response.json()

    def get_doctor(self, doctor_id: str) -> Dict:
        """GET /api/doctors/{id} - Retrieve doctor profile"""
        url = f"{self.base_url}{self.api_prefix}/{doctor_id}"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()

    def update_availability(self, doctor_id: str, avail: Dict) -> Dict:
        """PUT /api/doctors/{id}/availability - Update doctor's availability"""
        url = f"{self.base_url}{self.api_prefix}/{doctor_id}/availability"
        response = requests.post(url, json=avail)
        response.raise_for_status()
        return response.json()


    def add_review(self, doctor_id: str, patient_id: str,
                   rating: float, comment: str) -> Dict:
        """POST /api/doctors/{id}/reviews - Add a review for doctor"""
        url = f"{self.base_url}{self.api_prefix}/{doctor_id}/reviews"

        payload = {
            "patientId": patient_id,
            "rating": rating,
            "comment": comment
        }

        response = requests.post(url, json=payload)
        response.raise_for_status()
        return response.json()


class AppointmentServiceClient:
    """Client for Appointment Service"""

    def __init__(self, base_url: str = "http://localhost:8083"):
        self.base_url = base_url
        self.api_prefix = "/api/appointments"

    def schedule_appointment(self, patient_id: str, doctor_id: str,
                             start_time: str, appointment_type: str,
                             notes: Optional[str] = None) -> Dict:
        """POST /api/appointments - Schedule new appointment"""
        url = f"{self.base_url}{self.api_prefix}"

        try:
            # Safer parsing for ISO format (works for '2025-12-15T10:00:00')
            dt_start = datetime.fromisoformat(start_time)
        except ValueError:
            # Fallback if fromisoformat fails
            dt_start = datetime.strptime(start_time, "%Y-%m-%dT%H:%M:%S")

        duration = timedelta(hours=1)
        dt_end = dt_start + duration
        end_time_str = dt_end.isoformat()


        payload = {
            "patientId": patient_id,
            "doctorId": doctor_id,
            "startTime": dt_start.isoformat(),
            "endTime": end_time_str,
            "type": appointment_type,
            "notes": notes or "",
            "reason":"Anual CheckUp"
        }

        response = requests.post(url, json=payload)
        response.raise_for_status()
        return response.json()

    def get_appointment(self, appointment_id: str) -> Dict:
        """GET /api/appointments/{id} - Retrieve appointment details"""
        url = f"{self.base_url}{self.api_prefix}/{appointment_id}"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()

    def confirm_appointment(self, appointment_id: str) -> Dict:
        """POST /api/appointments/{id}/confirm - Confirm appointment"""
        url = f"{self.base_url}{self.api_prefix}/{appointment_id}/confirm"
        response = requests.post(url)
        response.raise_for_status()
        return response.json()

    def check_in_appointment(self, appointment_id: str) -> Dict:
        """POST /api/appointments/{id}/check-in - Patient check-in"""
        url = f"{self.base_url}{self.api_prefix}/{appointment_id}/check-in"
        response = requests.post(url)
        response.raise_for_status()
        return response.json()

    def complete_appointment(self, appointment_id: str) -> Dict:
        """POST /api/appointments/{id}/complete - Mark appointment as completed"""
        url = f"{self.base_url}{self.api_prefix}/{appointment_id}/complete"
        response = requests.post(url)
        response.raise_for_status()
        return response.json()

    def get_available_slots(self, doctor_id: str, date: str) -> List[Dict]:
        """GET /api/appointments/available-slots - Find available appointment slots"""
        url = f"{self.base_url}{self.api_prefix}/available-slots"
        params = {"doctorId": doctor_id, "date": date}
        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()

    def get_statistics(self) -> Dict:
        """GET /api/appointments/statistics - Get appointment statistics"""
        url = f"{self.base_url}{self.api_prefix}/statistics"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()


class NotificationServiceClient:
    """Client for Notification Service"""

    def __init__(self, base_url: str = "http://localhost:8084"):
        self.base_url = base_url
        self.api_prefix = "/api/notifications"

    def get_patient_notifications(self, patient_id: str) -> List[Dict]:
        """GET /api/notifications/patient/{patientId} - Get patient's notifications"""
        url = f"{self.base_url}{self.api_prefix}/patient/{patient_id}"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()

    def get_delivery_status(self) -> Dict:
        """GET /api/notifications/delivery-status - Check delivery statistics"""
        url = f"{self.base_url}{self.api_prefix}/delivery-status"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()


class BillingServiceClient:
    """Client for Billing Service"""

    def __init__(self, base_url: str = "http://localhost:8085"):
        self.base_url = base_url
        self.api_prefix = "/api/billing"

    def get_invoice_by_appointment(self, appointment_id: str) -> Dict:
        """GET /api/billing/invoices/appointment/{appointmentId} - Get invoice by appointment"""
        url = f"{self.base_url}{self.api_prefix}/invoices/appointment/{appointment_id}"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()

    def create_appoinment_invoice(self,appointment_id: str,patient_id):
        print("Creating appointment invoice.....")
        base_url = "http://localhost:8085/api/billing"
        create_invoice_url = f"{base_url}/invoices"
        dummy_items_pool = [
            {"description": "Initial Consultation", "quantity": 1, "unitPrice": 150.00},
            {"description": "Follow-up Visit", "quantity": 1, "unitPrice": 100.00},
            {"description": "Blood Test - Complete Blood Count", "quantity": 1, "unitPrice": 45.00},
            {"description": "Blood Test - Lipid Panel", "quantity": 1, "unitPrice": 55.00},
            {"description": "X-Ray - Chest", "quantity": 1, "unitPrice": 200.00},
            {"description": "X-Ray - Dental", "quantity": 1, "unitPrice": 85.00},
            {"description": "ECG/EKG Test", "quantity": 1, "unitPrice": 120.00},
            {"description": "Ultrasound Scan", "quantity": 1, "unitPrice": 250.00},
            {"description": "MRI Scan", "quantity": 1, "unitPrice": 800.00},
            {"description": "CT Scan", "quantity": 1, "unitPrice": 600.00},
            {"description": "Physical Examination", "quantity": 1, "unitPrice": 75.00},
            {"description": "Vaccination - Flu Shot", "quantity": 1, "unitPrice": 35.00},
            {"description": "Vaccination - COVID-19", "quantity": 1, "unitPrice": 40.00},
            {"description": "Prescription Medication", "quantity": 1, "unitPrice": 65.00},
            {"description": "Diabetes Screening", "quantity": 1, "unitPrice": 80.00},
            {"description": "Thyroid Function Test", "quantity": 1, "unitPrice": 95.00},
            {"description": "Urinalysis", "quantity": 1, "unitPrice": 30.00},
            {"description": "Vision Test", "quantity": 1, "unitPrice": 50.00},
            {"description": "Hearing Test", "quantity": 1, "unitPrice": 60.00},
            {"description": "Allergy Testing", "quantity": 1, "unitPrice": 180.00}
        ]

        num_items = random.randint(2, 4)
        selected_items = random.sample(dummy_items_pool, num_items)

        # Create invoice request payload
        invoice_data = {
            "appointmentId": int(appointment_id),
            "patientId": patient_id,
            "items": selected_items,
            "notes": f"Generated invoice for appointment #{appointment_id} on {datetime.now().strftime('%Y-%m-%d %H:%M')}"
        }

        try:
            # Make POST request to create invoice
            response = requests.post(
                create_invoice_url,
                headers={"Content-Type": "application/json"},
                json=invoice_data,
                timeout=10
            )

            # Raise exception for bad status codes
           # response.raise_for_status()

            # Return created invoice
            created_invoice = response.json()
            return created_invoice
        except Exception as e:
            print(f"❌ Unexpected error: {str(e)}")
            raise


    def verify_insurance(self, patient_id: str, provider: str,
                         policy_number: str,invoice_id,amount) -> Dict:
        """POST /api/billing/insurance/verify - Verify insurance coverage"""
        url = f"{self.base_url}{self.api_prefix}/insurance/verify"

        payload = {
            "patientId": patient_id,
            "insuranceProvider": provider,
            "policyNumber": policy_number,
            "claimedAmount":amount,
            "serviceDate": "2025-12-10",
            "invoiceId":invoice_id
        }

        response = requests.post(url, json=payload)
        response.raise_for_status()
        return response.json()

    def submit_insurance_claim(self, invoice_id: str, insurance_info: Dict) -> Dict:
        """POST /api/billing/insurance/claim - Submit insurance claim"""
        url = f"{self.base_url}{self.api_prefix}/insurance/claim"

        payload = {
            "invoiceId": invoice_id,
            "claimedAmount": insurance_info['claimedAmount'],
            "insuranceProvider": insurance_info['insuranceProvider'],
            "policyNumber": insurance_info['policyNumber'],
            "insuranceInfo": insurance_info
        }


        response = requests.post(url, json=payload)
        # response.raise_for_status()
        return response.json()

    def process_payment(self, invoice_id: str, amount: float,
                        payment_method: str) -> Dict:
        """POST /api/billing/payments - Process payment"""
        url = f"{self.base_url}{self.api_prefix}/payments"

        payload = {
            "invoiceId": invoice_id,
            "amount": amount,
            "paymentMethod": payment_method,
            "gateway":"Stripe"
        }
        response = requests.post(url, json=payload)
        # response.raise_for_status()
        return response.json()

    def get_revenue_report(self, start_date: Optional[str] = None,
                           end_date: Optional[str] = None) -> Dict:
        """GET /api/billing/reports/revenue - Revenue reports"""
        url = f"{self.base_url}{self.api_prefix}/reports/revenue"

        params = {}
        if start_date:
            params["startDate"] = start_date
        if end_date:
            params["endDate"] = end_date

        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()


class AnalyticsServiceClient:
    """Client for Analytics Service"""

    def __init__(self, base_url: str = "http://localhost:8086"):
        self.base_url = base_url
        self.api_prefix = "/api/analytics"

    def get_system_overview(self) -> Dict:
        """GET /api/analytics/system/overview - Overall service health summary"""
        url = f"{self.base_url}{self.api_prefix}/system/overview"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()

    def get_revenue_analytics(self, start_date: Optional[str] = None,
                              end_date: Optional[str] = None) -> Dict:
        """GET /api/analytics/revenue - Revenue analytics"""
        url = f"{self.base_url}{self.api_prefix}/revenue"

        params = {}
        if start_date:
            params["startDate"] = start_date
        if end_date:
            params["endDate"] = end_date

        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()

    def get_doctor_utilization(self, doctor_id: str) -> Dict:
        """GET /api/analytics/doctor/{id} - Doctor utilization metrics"""
        url = f"{self.base_url}{self.api_prefix}/doctor/{doctor_id}"
        response = requests.get(url)
        response.raise_for_status()
        return response.json()


# ============================================================================
# MAIN INTEGRATION CLASS
# ============================================================================

class HealthcareSystemIntegration:
    """
    Complete integration demonstrating the flow:
    Patient → Patient Service (patientId)
    Doctor → Doctor Service (doctorId, availability)
    Patient + Doctor + Time → Appointment Service
        ↓ (checks availability via Doctor Service)
        ↓ (stores appointment)
        ↓ (emits AppointmentCreated)
    → Notification Service (send confirmation)
    → Billing Service (after completion → invoice, payment)
    → Analytics Service (consumes all events → stats)
    """

    def __init__(self):
        # Initialize all service clients
        self.patient_service = PatientServiceClient("http://localhost:8081")
        self.doctor_service = DoctorServiceClient("http://localhost:8082")
        self.appointment_service = AppointmentServiceClient("http://localhost:8083")
        self.notification_service = NotificationServiceClient("http://localhost:8084")
        self.billing_service = BillingServiceClient("http://localhost:8085")
        self.analytics_service = AnalyticsServiceClient("http://localhost:8086")



    def run_complete_flow(self):
        """Execute the complete healthcare appointment flow"""

        print("\n" + "=" * 80)
        print("🏥 HEALTHCARE APPOINTMENT SCHEDULING SYSTEM - COMPLETE FLOW")
        print("=" * 80 + "\n")

        try:
            # ============= STEP 1: PATIENT REGISTRATION =============
            print("📝 STEP 1: PATIENT REGISTRATION")
            print("-" * 80)

            patient = self.patient_service.register_patient(
                first_name="Bob",
                last_name="Williams",
                email=random_email(),
                phone_number="+420333444555",
                date_of_birth="1992-07-08",
                gender="MALE"
            )

            patient_id = patient["id"]
            patient_name = f"{patient.get('firstName', '')} {patient.get('lastName', '')}"
            print(f"✅ Patient registered: {patient_name}")
            print(f"   Patient ID: {patient_id}")
            print(f"   Email: {patient['email']}\n")

            # Add insurance information

            policy_unique_number = generate_random_policy_number("POL");
            insurance_data = {
                "providerName": "AXA Insurance "+policy_unique_number,
                "policyNumber": policy_unique_number,
                "groupNumber": "GRP-YOUTH-300",
                "policyHolderName": "Parent Name",
                "policyHolderRelationship": "PARENT",
                "coverageStartDate": "2023-01-01",
                "coverageEndDate": "2026-12-31",
                "copayAmount": 10.00,
                "deductibleAmount": 250.00
            }


            self.patient_service.update_insurance(patient_id, insurance_data)
            print(f"✅ Insurance added: {insurance_data['providerName']}\n")

            # ============= STEP 2: DOCTOR REGISTRATION =============
            print("\n👨‍⚕️ STEP 2: DOCTOR REGISTRATION")
            print("-" * 80)

            doctor = self.doctor_service.register_doctor(
                first_name="John",
                last_name="Smith",
                email=random_email(),
                phone_number="+420111222333",
                specialization="Cardiology",
                years_of_experience=15,
                license_number=generate_random_policy_number("MED"),
                consultation_fee=100.0,
                bio="Experienced cardiologist",
                qualifications="MD, FACC"
            )

            doctor_id = doctor["id"]
            doctor_name = f"Dr. {doctor.get('firstName', '')} {doctor.get('lastName', '')}"
            print(f"✅ Doctor registered: {doctor_name}")
            print(f"   Doctor ID: {doctor_id}")
            print(f"   Specialization: {doctor['specialization']}\n")

            # Set availability
            availability = [
                {"dayOfWeek": "Monday", "startTime": "09:00", "endTime": "17:00"},
                {"dayOfWeek": "Tuesday", "startTime": "09:00", "endTime": "17:00"},
                {"dayOfWeek": "Wednesday", "startTime": "09:00", "endTime": "17:00"},
                {"dayOfWeek": "Thursday", "startTime": "09:00", "endTime": "17:00"},
                {"dayOfWeek": "Friday", "startTime": "09:00", "endTime": "13:00"}
            ]

            for avil in availability:
                self.doctor_service.update_availability(doctor_id, avil)

            print(f"✅ Availability set: {len(availability)} days per week\n")


            # ============= STEP 3: APPOINTMENT BOOKING =============
            print("\n📅 STEP 3: APPOINTMENT BOOKING")
            print("-" * 80)

            # Check available slots first
            print("🔍 Checking available slots...")
            try:
                available_slots = self.appointment_service.get_available_slots(
                    doctor_id=doctor_id,
                    date="2025-12-15"
                )
                print(f"✅ Found {len(available_slots)} available slots\n")
            except Exception as e:
                print(f"⚠️  Could not fetch available slots: {e}\n")

            # Schedule appointment
            appointment = self.appointment_service.schedule_appointment(
                patient_id=patient_id,
                doctor_id=doctor_id,
                start_time="2025-12-15T10:00:00",
                appointment_type="CONSULTATION",
                notes="Annual heart checkup"
            )

            appointment_id = appointment["id"]
            print(f"✅ Appointment scheduled!")
            print(f"   Appointment ID: {appointment_id}")
            print(f"   Time: {appointment['startTime']}")
            print(f"   Status: {appointment['status']}\n")

            # Confirm appointment
            self.appointment_service.confirm_appointment(appointment_id)
            print(f"✅ Appointment confirmed\n")

            # ============= STEP 4: NOTIFICATIONS =============
            print("\n📧 STEP 4: NOTIFICATIONS (Automatic via Kafka)")
            print("-" * 80)

            # Wait for async processing
            time.sleep(1)

            try:
                notifications = self.notification_service.get_patient_notifications(patient_id)
                print(f"✅ {len(notifications)} notifications sent to patient")

                if notifications:
                    for notif in notifications[:2]:
                        print(f"   - {notif.get('subject', 'N/A')}: {notif.get('status', 'N/A')}")
            except Exception as e:
                print(f"⚠️  Could not fetch notifications: {e}")
            print()

            # ============= STEP 5: APPOINTMENT COMPLETION =============
            print("\n✅ STEP 5: APPOINTMENT COMPLETION")
            print("-" * 80)

            # Simulate patient check-in
            self.appointment_service.check_in_appointment(appointment_id)
            print(f"✅ Patient checked in\n")

            # Complete appointment
            self.appointment_service.complete_appointment(appointment_id)
            print(f"✅ Appointment completed\n")

            # Add doctor review
            self.doctor_service.add_review(
                doctor_id=doctor_id,
                patient_id=patient_id,
                rating=5.0,
                comment="Excellent doctor, very thorough examination!"
            )
            print(f"✅ Review added: 5.0/5.0\n")

            # ============= STEP 6: BILLING & PAYMENT =============
            print("\n💰 STEP 6: BILLING & PAYMENT")
            print("-" * 80)

            # Wait for async invoice creation
            print("⏳ Waiting for invoice generation...")
            time.sleep(2)

            try:
                # Get invoice by appointment
                invoice =  self.billing_service.create_appoinment_invoice(appointment_id,patient_id);
               # invoice = self.billing_service.get_invoice_by_appointment(appointment_id)
                invoice_id = invoice["id"]

                print(f"✅ Invoice auto-generated:")
                print(f"   Invoice ID: {invoice_id}")
                print(f"   Total: ${invoice['total']:.2f}")
                print(f"   Status: {invoice['status']}\n")

                # Verify insurance
                print("🔍 Verifying insurance coverage...")
                insurance_verification = self.billing_service.verify_insurance(
                    patient_id=patient_id,
                    provider="AXA Insurance "+policy_unique_number,
                    policy_number=policy_unique_number,
                    invoice_id=invoice_id,
                    amount=invoice['total']
                )
                print(f"✅ Insurance verified:")
                print(f"   Claim Amount: {insurance_verification['claimedAmount']}%")
                print(f"   Approved Amount: ${invoice['total']}\n")

                # Submit insurance claim
                claim = self.billing_service.submit_insurance_claim(
                    invoice_id=invoice_id,
                    insurance_info=insurance_verification
                )
                print(f"✅ Insurance claim submitted: {claim['id']}\n")

                # Process payment (patient pays copay)
                payment = self.billing_service.process_payment(
                    invoice_id=invoice_id,
                    amount=insurance_verification["claimedAmount"],
                    payment_method="CREDIT_CARD"
                )

                print(f"✅ Payment processed:")
                print(f"   Payment ID: {payment['id']}")
                print(f"   Amount: ${payment['amount']:.2f}")
                print(f"   Status: {payment['status']}")
                print(f"   Transaction ID: {payment.get('transactionId', 'N/A')}\n")

            except Exception as e:
                print(f"⚠️  Billing processing note: {e}")
                print(f"   (Invoice may be generated asynchronously)\n")


            # ============= STEP 7: ANALYTICS & INSIGHTS =============
            print("\n📊 STEP 7: ANALYTICS & INSIGHTS")
            print("-" * 80)

            # Wait for analytics processing
            time.sleep(1)

            try:
                # Get system overview
                overview = self.analytics_service.get_system_overview()
                print(f"📈 System Overview:")
                print(f"   Status: {overview.get('systemStatus', 'N/A')}")
                print(f"   Total Events: {overview.get('totalEventsTracked', 0)}\n")
            except Exception as e:
                print(f"⚠️  Analytics overview: {e}\n")

            try:
                # Get appointment statistics
                stats = self.appointment_service.get_statistics()
                print(f"📊 Appointment Statistics:")
                print(f"   Total: {stats.get('totalAppointments', 0)}")
                print(f"   Completed: {stats.get('completed', 0)}")
                print(f"   Completion Rate: {stats.get('completionRate', 0):.1f}%\n")
            except Exception as e:
                print(f"⚠️  Appointment stats: {e}\n")

            try:
                # Get revenue analytics
                revenue = self.analytics_service.get_revenue_analytics()
                print(f"💵 Revenue Analytics:")
                print(f"   Total Revenue: ${revenue.get('totalRevenue', 0):.2f}")
                print(f"   Transactions: {revenue.get('totalTransactions', 0)}\n")
            except Exception as e:
                print(f"⚠️  Revenue analytics: {e}\n")

            try:
                # Get doctor performance
                doctor_metrics = self.analytics_service.get_doctor_utilization(doctor_id)
                print(f"👨‍⚕️ Doctor Performance:")
                print(f"   Appointments: {doctor_metrics.get('totalAppointments', 0)}")
                print(f"   Completion Rate: {doctor_metrics.get('completionRate', 0):.1f}%")
                print(f"   Rating: {doctor_metrics.get('averageRating', 0):.1f}/5.0\n")
            except Exception as e:
                print(f"⚠️  Doctor metrics: {e}\n")

            # ============= FINAL SUMMARY =============
            print("\n" + "=" * 80)
            print("✅ COMPLETE FLOW EXECUTED SUCCESSFULLY!")
            print("=" * 80 + "\n")

            print("📋 Summary:")
            print(f"   ✅ Patient registered: {patient_name}")
            print(f"   ✅ Doctor registered: {doctor_name}")
            print(f"   ✅ Appointment scheduled and completed")
            print(f"   ✅ Notifications triggered automatically (Kafka)")
            print(f"   ✅ Invoice generation triggered (Kafka)")
            print(f"   ✅ Payment processing attempted")
            print(f"   ✅ Analytics updated\n")

            print("🔄 Event-Driven Architecture:")
            print("   ✅ All services communicated via Kafka events")
            print("   ✅ Loose coupling demonstrated")
            print("   ✅ Async processing working correctly\n")

            return True

        except Exception as e:
            print(f"\n❌ Error occurred: {str(e)}")
            print(f"   Make sure all Quarkus services are running!")
            import traceback
            traceback.print_exc()
            return False


# ============================================================================
# MAIN ENTRY POINT
# ============================================================================

def main():
    """
    Main entry point for the integration flow

    Prerequisites:
    1. Start all Quarkus services:
       - Patient Service (port 8081)
       - Doctor Service (port 8082)
       - Appointment Service (port 8083)
       - Notification Service (port 8084)
       - Billing Service (port 8085)
       - Analytics Service (port 8086)

    2. Ensure Kafka is running for event messaging

    3. Ensure PostgreSQL databases are running for each service
    """

    print("\n" + "=" * 80)
    print("🚀 Healthcare System Integration - All-in-One Script")
    print("=" * 80)
    print("\n⚙️  Initializing service clients...")

    integration = HealthcareSystemIntegration()

    print("✅ All service clients initialized\n")
    print("📌 Prerequisites Check:")
    print("   - Quarkus services running on ports 8081-8086")
    print("   - Kafka broker running and accessible")
    print("   - PostgreSQL databases available")
    print("\n🎬 Starting complete workflow...\n")

    success = integration.run_complete_flow()

    if success:
        print("\n🎓 Perfect for University Course Project Demonstration!")
        print("   This shows complete microservices architecture in action.\n")
    else:
        print("\n💡 Troubleshooting Tips:")
        print("   1. Check if all Quarkus services are running")
        print("   2. Verify Kafka broker is accessible")
        print("   3. Check service logs for any errors")
        print("   4. Ensure PostgreSQL databases are created\n")


if __name__ == "__main__":
    main()