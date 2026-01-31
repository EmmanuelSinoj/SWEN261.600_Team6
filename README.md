# SWEN261.600_Team6
SWEN261_600 Term Project Repository
Group 6
Leen Malkawi (772000938)
Saood Al Jerman (771003924)
Ahmed Abd Elaal (764003579)
Emmanuel Sinoj Periyil (396004652)

Product Vision Statement:
FOR university students and academic staff
WHO needs an efficient way to browse courses and manage enrollments
THE UniEnroll IS a web-based academic platform
THAT simplifies course discovery, enrollment, and administration while reducing manual work and improving the academic experience
UNLIKE manual registration processes or outdated campus systems
OUR PRODUCT is intuitive, reliable, and designed specifically for modern universities

Student:
1.	View all available courses
2.	Search and filter courses by criteria
    a.	COURSE LEVEL(100, 200, 300)
    b.	COURSE CODE (CSEC, SWEN)
    c.	PROFESSORS
    d.	IN-PERSON OR ONLINE
    e.	STATUS (AVAILABLE, WAITLIST, FULL)
    f.	DAYS & TIMING
3.	Add courses to a personal enrolment basket
4.	Remove courses from the basket
5.	Commit enrolment
6.	Enrollment basket is preserved across logouts and system restarts
Admin:
1.	CRUD on the courses.
2.	Cannot view or access any student enrollment baskets
3.	All unauthorized attempts to access student data are denied

Data Persistence
1.	Course catalog and student enrollment data are stored in a database
2.	Application state is fully restored after logout, browser close, or server restart
3.	Student enrollment baskets persist until committed
