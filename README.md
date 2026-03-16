# UNIENROLL – Academic Course Enrollment Platform

---

## Team Members
- Leen Malkawi (ID: 772000938)  
- Saood Al Jerman (ID: 771003924)  
- Ahmed Abd Elaal (ID: 764003579)  
- Emmanuel Sinoj Periyil (ID: 396005742)  
- Mohammad Hesham (ID: 784003683)  
- Ammar Ahmed (ID: 377004813)  

---

## Product Vision Statement

**FOR** university students and academic staff  
**WHO** need an efficient way to browse courses and manage enrollments  
**THE UniEnroll** is a web-based academic platform  
**THAT** simplifies course discovery, enrollment, and administration while reducing manual work and improving the academic experience  
**UNLIKE** manual registration processes or outdated campus systems  
**OUR PRODUCT** is intuitive, reliable, and designed specifically for online universities  

---

## Key Features

### Student Capabilities

Students using UniEnroll can:

- View all available courses offered in the current semester  
- Search and filter courses using multiple criteria:
  - Course Level (100, 200, 300)
  - Course Code (e.g., CSEC, SWEN)
  - Professor Name
  - Delivery Mode (In-Person, Online, Hybrid)
  - Enrollment Status (Available, Waitlist, Full)
  - Days & Timing
- Add courses to a personal enrollment basket  
- Remove courses from the basket before confirmation  
- Commit and finalize enrollment  
- Have their enrollment basket preserved across logouts and system restarts  

---

### Administrator Capabilities

Administrators are able to:

- Perform full CRUD operations on courses (Create, Read, Update, Delete)  
- Manage system data without accessing or viewing individual student enrollment baskets  
- Ensure all unauthorized attempts to access student data are denied  

---

## System Goals

- Reduce manual registration workload  
- Provide a clear and user-friendly interface  
- Maintain data security and student privacy  
- Ensure reliability across academic terms  

---

## Project Management & Collaboration

**Methodology:** Agile / Scrum-inspired workflow  
**Tool Used:** Trello  

Trello is used to track:

- User stories and requirements  
- Sprint tasks and assignments  
- Bug reports and feature requests  
- Progress across development iterations  

---

## Data Persistence

- Course catalog and student enrollment data are stored in a database  
- Application state is fully restored after logout, browser close, or server restart  
- Student enrollment baskets persist until committed  

---

## Assignment 1 Progress

### Completed:
- [x] Domain Analysis & Class Diagram  
- [x] Glossary of Terms  
- [x] Functional Requirements Document  
- [x] Traceability Matrix  
- [x] User Stories Definition (Total: 15 stories)  
- [x] Sprint 1 Implementation (3 stories completed)  

---

## Sprint 1 Details

**Sprint Goal:**  
Implement core user functionalities including login, logout and role based redirect functiontionalities. Implement admin functionalities including adding, deleting, updating, activating and deactivating student accounts.

**Duration:**  
14th February 2026 to 25th February 2026

**Completed Stories:**
1. US-001: User Login & Role Redirect  
2. US-002: Role-Based Access Control (RBAC)  
3. US-003: Manage Student Accounts

---

## Sprint 2 Details

**Sprint Goal:** Equip administrators with tools to build and manage the course catalog, and empower students to browse, filter, and plan their schedules using a persistent shopping cart and real-time seat availability indicators.

**Duration:** 26th February 2026 to 15th March 2026

**Completed Stories:**
1. US-04: Create Course & Schedule Sections
2. US-05: Advanced Search & Filter
3. US-06: View Course Availability
4. US-07: Manage Enrollment Cart

---

## Project Progress: Completed User Stories & Features

### Features Implemented So Far
The UniEnroll platform now has a solid foundation for both Administrative and Student workflows. 

* **Security & Access:** Secure user login, session management, and Role-Based Access Control (RBAC) correctly routing Students and Admins to their respective dashboards.
* **Admin Capabilities:** Administrators can fully manage student accounts (add, update, delete, activate/deactivate) and control the academic catalog by creating, updating, and deleting Courses and scheduling specific Sections.
* **Student Experience:** Students can browse the catalog using an advanced search and filter system (by keyword, level, professor, delivery mode, and days). They can see real-time status badges (OPEN, WAITLIST, FULL) based on current enrollments, and draft their upcoming schedule using a persistent database-backed Shopping Cart.

### Complete List of Delivered User Stories (Sprints 1 & 2)
- [x] **US-01:** User Login & Role Redirect  
- [x] **US-02:** Role-Based Access Control (RBAC)  
- [x] **US-03:** Manage Student Accounts
- [x] **US-04:** Create Course & Schedule Sections
- [x] **US-05:** Advanced Search & Filter
- [x] **US-06:** View Course Availability
- [x] **US-07:** Manage Enrollment Cart

---

## Links

- **GitHub Repository:**  
  https://github.com/EmmanuelSinoj/SWEN261.600_Team6  

- **Trello Board:**  
  https://trello.com/b/X8e5JQ4G/swen261600team6

- **Functional Requirements:**  
  https://docs.google.com/document/d/1jg0YROrAA-XOZv-E0-FpQozw2gaLYb8TqP4JL_sXltU  

- **Traceability Matrix:**  
  https://docs.google.com/spreadsheets/d/1DwKVT0ptUckvCwcQvSxi3y6QgkCcqvNV6qVV8ap2Cvw  
