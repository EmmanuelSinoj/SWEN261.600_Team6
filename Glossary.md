# UniEnroll System Glossary

## Table of Contents
- [1. Domain Entities & Business Terms](#1-domain-entities--business-terms)
- [2. User Roles](#2-user-roles)
- [3. Technical Implementation Terms](#3-technical-implementation-terms)
- [4. Acronyms & Abbreviations](#4-acronyms--abbreviations)

---

## 1. Domain Entities & Business Terms

### Section

| Attribute | Value |
|-----------|-------|
| **Definition** | A specific, scheduled instance of a Course offered in the current semester. Unlike a generic course subject, a Section has specific days, times, and a room number. |
| **Synonyms** | Class Instance, Scheduled Class |
| **Related Terms** | Course, Catalog, Timetable |
| **Attributes** | `days`, `startTime`, `endTime`, `room`, `capacity`, `enrolledCount` |

**Business Rules:**
- **BR-002**: Cannot exceed maximum seat capacity.
- **BR-008**: Cannot overlap in time with another section in a student's schedule.

**Example:** "Introduction to Java - Section 01 (Mon/Wed 9:00 AM)"

---

### Course

| Attribute | Value |
|-----------|-------|
| **Definition** | The academic subject defined in the university curriculum. It acts as a template for Sections but contains no scheduling data itself. |
| **Synonyms** | Subject, Module, Catalog Entry |
| **Related Terms** | Section, Curriculum |
| **Attributes** | `courseCode`, `title`, `description`, `credits` (e.g., 3.0) |

**Business Rules:**
- **BR-001**: Course Code must be unique.

**Example:** "CS101: Introduction to Computer Science (3 Credits)"

---

### Enrollment Basket (Shopping Cart)

| Attribute | Value |
|-----------|-------|
| **Definition** | A persistent staging area where a Student adds Sections they are interested in. It allows students to plan their schedule and check for conflicts before officially committing to enrollment. |
| **Synonyms** | Cart, Course Bag, Selection List |
| **Related Terms** | Student, Commit |
| **Attributes** | `basketId`, `studentId`, `lastModified` |

**Business Rules:**
- **BR-006**: Content must persist across sessions (if user logs out).
- **BR-007**: The sum of credits in the Basket + Active Enrollments cannot exceed 18.

**Example:** A student adds "Math 101" to their cart but is still deciding between "History" and "Physics".

---

### Commit

| Attribute | Value |
|-----------|-------|
| **Definition** | The transactional action of finalizing the items in the Enrollment Basket. This moves data from the temporary "Basket" state to the permanent "Enrollment" state and updates seat counts. |
| **Synonyms** | Checkout, Finalize, Register |
| **Related Terms** | Enrollment Basket, Enrollment Record |
| **Attributes** | N/A (Action) |

**Business Rules:**
- **FR-031**: Fails if the student exceeds the Credit Limit.
- **FR-032**: Fails if a Time Conflict is detected.

**Example:** A student clicks "Finalize Enrollment," and the system confirms their seat in 3 classes.

---

### Credit Limit

| Attribute | Value |
|-----------|-------|
| **Definition** | The maximum number of academic credits a student is allowed to take in a single semester. |
| **Synonyms** | Max Load, Credit Cap |
| **Related Terms** | Course, Commit |
| **Attributes** | Default is 18, but Admin can change it per student |

**Business Rules:**
- **BR-007**: Total Credits &lt;= 18.

**Example:** If a student has 15 credits enrolled, they cannot add a 4-credit Physics course.

---

### Timetable

| Attribute | Value |
|-----------|-------|
| **Definition** | A visual representation of a student's confirmed enrollments, displayed as a weekly grid. |
| **Synonyms** | Schedule View, Calendar |
| **Related Terms** | Enrollment, Section |
| **Attributes** | Derived from `Section.day` and `Section.time` |

**Business Rules:**
- **FR-050**: Only displays confirmed enrollments, not items currently in the Basket.

**Example:** A grid showing "CS101" blocks on Monday and Wednesday mornings.

---

## 2. User Roles

### Administrator

| Attribute | Value |
|-----------|-------|
| **Definition** | An authorized academic staff member responsible for creating Courses and scheduling Sections. |
| **Synonyms** | Owner, Registrar, Staff |
| **Related Terms** | Course Management |
| **Attributes** | `username`, `passwordHash`, `role='ADMINISTRATOR'` |

**Business Rules:**
- **FR-043**: Can view student rosters but cannot modify student baskets.

**Example:** Admin "Alice" cancels a section of History due to low enrollment.

---

### Student

| Attribute | Value |
|-----------|-------|
| **Definition** | A registered university user who accesses the system to browse the catalog, manage their cart, and enroll in classes. |
| **Synonyms** | Helper, Learner, User |
| **Related Terms** | Enrollment Basket, Transcript |
| **Attributes** | `studentId`, `name`, `email`, `role='STUDENT'` |

**Business Rules:**
- **BR-003**: Cannot enroll in the same course twice in one term.

**Example:** Student "Bob" drops a class to make room for a different elective.

---

## 3. Technical Implementation Terms

### Persistence

| Attribute | Value |
|-----------|-------|
| **Definition** | The characteristic of data surviving after the process that created it has ended. |
| **Synonyms** | Data Storage |
| **Related Terms** | Database, Session |
| **Attributes** | N/A |

**Business Rules:**
- **NFR-020**: Basket data is saved to MariaDB immediately upon addition.

**Example:** A student adds a course on their phone, logs out, and sees it later on their laptop.

---

### Transaction

| Attribute | Value |
|-----------|-------|
| **Definition** | A set of database operations that must either all succeed or all fail to ensure data integrity. |
| **Synonyms** | Atomic Operation |
| **Related Terms** | Commit, Rollback |
| **Attributes** | N/A |

**Business Rules:**
- **C-006**: The Enrollment Commit process is transactional to prevent "over-booking" a full class.

**Example:** If the system crashes while moving a course from Basket to Enrollment, the seat count is not incorrectly reduced.

---

### DTO (Data Transfer Object)

| Attribute | Value |
|-----------|-------|
| **Definition** | A plain Java object used to transfer data between the backend (Spring Boot) and frontend (Angular), usually to hide internal database structure. |
| **Synonyms** | Transfer Object |
| **Related Terms** | Controller, Service |
| **Attributes** | `courseName`, `timeSlot` (simplified view) |

**Business Rules:** N/A

**Example:** Sending only the "Course Title" and "Time" to the frontend, rather than the entire database record.

---

## 4. Acronyms & Abbreviations

| Acronym | Full Form | Description |
|---------|-----------|-------------|
| **API** | Application Programming Interface | Set of protocols for building software applications |
| **CRUD** | Create, Read, Update, Delete | Basic database operations |
| **REST** | Representational State Transfer | Architecture style for designing networked applications |
| **RBAC** | Role-Based Access Control | Security approach restricting system access based on user roles |
| **JSON** | JavaScript Object Notation | Lightweight data interchange format |
| **UI** | User Interface | The Angular frontend that users interact with |
| **DTO** | Data Transfer Object | Object that carries data between processes |
| **SQL** | Structured Query Language | Language for managing relational databases |
| **HTTP** | HyperText Transfer Protocol | Protocol for transferring data over the web |
| **CORS** | Cross-Origin Resource Sharing | Security feature controlling resource access between domains |
| **BCrypt** | Blowfish Crypt | Password hashing function based on Blowfish cipher |
| **JPA** | Java Persistence API | Java specification for managing relational data |
| **MVC** | Model-View-Controller | Software architectural pattern |

---

