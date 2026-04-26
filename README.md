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

**Duration:** 04th February 2026 to 15th March 2026

**Completed Stories:**
1. US-04: Create Course & Schedule Sections
2. US-05: Advanced Search & Filter
3. US-06: View Course Availability
4. US-07: Manage Enrollment Cart

### Contributions:

**Emmanuel:**
- **Student Dashboard UI:** Built the core frontend interface (student.mustache) featuring a responsive dark-theme layout, top navigation bar, and clean empty-state handling ({{^sections}}) for zero-result searches.
- **Advanced Search & Filter (US-05):** Engineered the frontend sidebar form and backend GET routing (StudentController.java) to process multi-parameter queries (including data sanitization like converting level 100 to prefix "1"). Added client-side JavaScript to retain and auto-select filter states after page reloads.
- **Real-Time Availability (US-06):** Implemented Mustache conditional logic ({{#isOpen}}, {{#isWaitlist}}, {{#isFull}}) to render color-coded course capacity badges dynamically based on the database's enrollment data.

**Leen**:

-  **Enrollment Cart Backend (US-07):** Implemented the backend logic for the enrollment cart using Cart.java, CartRepository.java, and EnrollmentCartService.java, enabling persistent cart storage in MariaDB and enforcing business rules such as preventing duplicate section additions and handling time conflicts.

- **Cart Routing Integration:** Updated StudentController.java to integrate cart-related endpoints and connect the frontend with the backend cart functionality.

**Saood:**

- **Course & Section Domain Modeling (US-04):** Designed and implemented the core backend entities (Course.java, Section.java), defining relationships between courses and their scheduled sections, including attributes such as course code, title, instructor, schedule, and capacity.

- **Business Logic Development:** Built CourseService.java to manage course and section operations (create, update, delete), enforcing constraints like valid scheduling data and section capacity handling.

- **Admin Dashboard UI (Mustache):** Developed the admin-courses.mustache page, providing administrators with an interface to create, edit, and delete courses and sections.

- **Testing & Validation:** Conducted end-to-end testing of course and section functionality, ensuring correct CRUD operations, business logic enforcement, and proper UI rendering.
  
*Ahmed:*

- *Repository Layer Implementation:* Created CourseRepository.java (and SectionRepository.java if applicable) using Spring Data JPA to handle database operations for courses and sections in MariaDB.
  
- *Admin Controller & Endpoints:* Implemented CourseAdminController.java with endpoints for creating, updating, and deleting courses and sections, including request validation and handling.
  
- *Dashboard Integration:* Connected backend data to the admin dashboard, enabling dynamic rendering of courses and their associated sections using Mustache templates.
  
- *Testing Support:* Assisted in testing course and section management features, verifying endpoint functionality, data persistence, and frontend-backend integration.
  
**Mohammad Hesham:**

-  **Glossary of Terms (Assignment 1 - Part A):** Created and structured the project glossary (Glossary.md), defining all domain-specific terms such as Section, Course, Enrollment Basket, and Commit, along with their attributes, related terms, and business rules.

-  **Technical & Acronym Definitions:** Included technical implementation terms (e.g., DTO, Transaction, Persistence) and acronyms (e.g., API, CRUD, REST, RBAC) as required in the assignment.

-  **Student Dashboard Enhancement (US-05 / US-06):** Updated and refined the student.mustache frontend to align with the advanced search and real-time availability features. Improved UI structure, layout consistency, and integration of dynamic data rendering from the backend.

-  **Cart UI Integration (US-07):** Integrated the enrollment cart view into the student dashboard by updating student.mustache and adding student-cart.mustache, ensuring seamless navigation between course browsing and cart management.

-  **Frontend-Backend Synchronization:** Ensured that Mustache templates correctly reflect backend-provided data (filters, course states, and cart interactions), enabling a consistent and responsive user experience across page reloads.

**Ammar:**

- Advanced Search & Filter Backend Support (US-05): Implemented search and filtering logic in SectionRepository.java using custom JPQL queries to allow filtering by course code, title, professor, delivery mode, level prefix, and day, enabling multi-criteria catalog search functionality.

- Course Availability Logic (US-06): Extended Section.java by adding capacity, enrollment count, and waitlist handling, along with helper methods to determine whether a section is OPEN, WAITLIST, or FULL, ensuring correct availability status is shown in the student catalog.

- Schedule Conflict Detection: Implemented time-conflict validation between sections based on meeting days and start/end times to support correct filtering results and prevent invalid schedule combinations.

- Catalog Filtering Integration: Ensured repository queries correctly interact with backend services so filtered results return accurate section data, including professor, schedule, delivery mode, and availability state.

- UI Template Contribution: Created coming-soon.mustache with navbar integration and consistent styling to match the student dashboard layout.
---

## Technology Stack
* **Backend:** Java with Spring Boot (Spring Web, Spring Data JPA, Spring Security)
* **Frontend:** HTML/CSS with Mustache Templating Engine
* **Database:** Relational Database (managed via Hibernate/JPA)

---

## Assignment 3 Progress & Sprint 3 Details

**Sprint Goal:** Enhance the UniEnroll platform with advanced enrollment logic, transaction safety, and automated waitlisting. Transitioned to a hybrid architecture incorporating RESTful endpoints alongside existing MVC patterns.

**Technology Stack Update:**
* **Backend:** Spring Boot with Spring Data JPA & MariaDB.
* **Architecture:** Hybrid MVC (Mustache) and REST (@RestController).
* **Data Handling:** Persistent database storage with simulated service-level relationship logic for complex transactions.

**Completed Stories & Individual Contributions:**
* **Saood Al Jerman:** completed **US-08A: Cart Validation & Conflict Warnings** (Implemented validation logic in Service layer and updated Cart UI).
* **Emmanuel Sinoj:** completed **US-08B: Secure Enrollment Checkout** (Developed transactional checkout logic and RESTful status endpoints).
* **Mohammad Hesham:** completed **US-09A: Drop Course** (Core Functionality - Created DELETE endpoints and updated student dashboard).
* **Leen Malkawi:** completed **US-09B: Automated Waitlist Processing** (Implemented background logic for seat reallocation).
* **Ammar Ahmed:** completed **US-10: View Timetable** (Built student schedule view using responsive HTML/CSS).
* **Ahmed Abd Elaal:** completed **US-12: View Enrolled Courses** (Implemented data retrieval and display for student enrollment history).

---

### Complete List of Delivered User Stories (Sprints 1, 2, & 3)
- [x] **US-01:** User Login & Role Redirect  
- [x] **US-02:** Role-Based Access Control (RBAC)  
- [x] **US-03:** Manage Student Accounts
- [x] **US-04:** Create Course & Schedule Sections
- [x] **US-05:** Advanced Search & Filter
- [x] **US-06:** View Course Availability
- [x] **US-07:** Manage Enrollment Cart
- [x] **US-08A:** Cart Validation & Conflict Warnings
- [x] **US-08B:** Secure Enrollment Checkout (Transaction)
- [x] **US-09A:** Drop Course (Core Functionality)
- [x] **US-09B:** Automated Waitlist Processing
- [x] **US-10:** View Timetable
- [x] **US-12:** View Enrolled Courses

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

- **Canva Presentation Link:**  
  https://canva.link/4qgj6faffunj5zp 
