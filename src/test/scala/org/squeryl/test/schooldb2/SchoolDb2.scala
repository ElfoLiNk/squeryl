package org.squeryl.test.schooldb2

import org.squeryl.test.PrimitiveTypeModeForTests._
import org.squeryl._
import dsl.{CompositeKey2, ManyToMany, ManyToOne, OneToMany}
import java.sql.Savepoint

import org.squeryl.framework._
import org.squeryl.test.PrimitiveTypeModeForTests

trait SchoolDb2Object {
  val id: Long = 0
}

object SchoolDb2 extends SchoolDb2

class Professor(val lastName: String, var bossId: Option[Long] = None) extends SchoolDb2Object {

  lazy val courses: Query[Course] with ManyToMany[Course, CourseAssignment] = SchoolDb2.courseAssignments.left(this)
}

class Course(val subjectId: Long) extends SchoolDb2Object {

  def this() = this(0)

  // Lets support the case where a course can have more than one professor
  lazy val professors: Query[Professor] with ManyToMany[Professor, CourseAssignment] = SchoolDb2.courseAssignments.right(this)

  lazy val students: Query[Student] with ManyToMany[Student, CourseSubscription] = SchoolDb2.courseSubscriptions.left(this)

  lazy val subject: ManyToOne[Subject] = SchoolDb2.subjectToCourses.right(this)
}

class Student(val firstName: String, val lastName: String) extends SchoolDb2Object {

  lazy val courses: Query[Course] with ManyToMany[Course, CourseSubscription] = SchoolDb2.courseSubscriptions.right(this)

  def fullName: CompositeKey2[String, String] = compositeKey(firstName, lastName)
}

class Subject(val name: String, val parentSubjectId: Option[Long]) extends SchoolDb2Object {

  lazy val courses: OneToMany[Course] = SchoolDb2.subjectToCourses.left(this)

  lazy val childSubjects: OneToMany[Subject] = SchoolDb2.subjectToParentSubject.left(this)

  lazy val parentSubject: ManyToOne[Subject] = SchoolDb2.subjectToParentSubject.right(this)
}

class CourseSubscription(val courseId: Long, val studentId: Long, var grade: Float)
    extends KeyedEntity[CompositeKey2[Long, Long]] {

  def id: CompositeKey2[Long, Long] = compositeKey(courseId, studentId)
}

class CourseAssignment(val courseId: Long, val professorId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {

  def id: CompositeKey2[Long, Long] = compositeKey(courseId, professorId)
}

case class Entry(text: String) extends KeyedEntity[Int] {
  val id: Int = 0
  // entryToComments is a one-to-many relation:
  lazy val comments: OneToMany[Comment] = SchoolDb2.entryToComments.left(this)
}

case class Comment(text: String, entryId: Int = 0, userId: Int = 0) extends KeyedEntity[Int] {
  val id: Int = 0
}

class ASTConstructionInterferenceA extends KeyedEntity[Long] {
  val id: Long = 0

  lazy val bs: OneToMany[ASTConstructionInterferenceB] = SchoolDb2.aToB.left(this)
}

class ASTConstructionInterferenceB(val aId: Long) extends KeyedEntity[Long] {
  val id: Long = 0

  val field1 = "abc"
  val field2: String = field1
}

class SchoolDb2 extends Schema {

  implicit object schoolDbObjectKED extends KeyedEntityDef[SchoolDb2Object, Long] {
    def getId(a: SchoolDb2Object): Long = a.id
    def isPersisted(a: SchoolDb2Object): Boolean = a.id > 0
    def idPropertyName                  = "id"
  }

  val entries: Table[Entry] = table[Entry]
  val comments: Table[Comment] = table[Comment]("commentz")

  val entryToComments: PrimitiveTypeModeForTests.OneToManyRelationImpl[Entry, Comment] =
    oneToManyRelation(entries, comments).via((e, c) => e.id === c.entryId)

  val professors: Table[Professor] = table[Professor]

  val students: Table[Student] = table[Student]

  on(students)(
    s =>
      declare(
        s.firstName is indexed,
        s.lastName defaultsTo "!",
        s.fullName is (unique, indexed),
        columns(s.id, s.firstName, s.lastName) are indexed
    )
  )

  val courses: Table[Course] = table[Course]

  val subjects: Table[Subject] = table[Subject]

  val courseAssignments: PrimitiveTypeModeForTests.ManyToManyRelationImpl[Professor, Course, CourseAssignment] =
    manyToManyRelation(professors, courses, "CourseAssignmentZ")
      .via[CourseAssignment]((p, c, a) => (p.id === a.professorId, a.courseId === c.id))

  val courseSubscriptions: PrimitiveTypeModeForTests.ManyToManyRelationImpl[Course, Student, CourseSubscription] =
    manyToManyRelation(courses, students)
      .via[CourseSubscription]((c, s, cs) => (cs.studentId === s.id, c.id === cs.courseId))

  val subjectToCourses: PrimitiveTypeModeForTests.OneToManyRelationImpl[Subject, Course] =
    oneToManyRelation(subjects, courses).via((s, c) => s.id === c.subjectId)

  val bossToProfessors: PrimitiveTypeModeForTests.OneToManyRelationImpl[Professor, Professor] =
    oneToManyRelation(professors, professors).via((boss, p) => boss.id === p.bossId)

  val subjectToParentSubject: PrimitiveTypeModeForTests.OneToManyRelationImpl[Subject, Subject] =
    oneToManyRelation(subjects, subjects).via(
      (subject, childSubject) => Option(subject.id) === childSubject.parentSubjectId
    )

  // the default constraint for all foreign keys in this schema :
  override def applyDefaultForeignKeyPolicy(foreignKeyDeclaration: ForeignKeyDeclaration): Unit =
    foreignKeyDeclaration.constrainReference

  //now we will redefine some of the foreign key constraints :
  //if we delete a subject, we want all courses to be deleted
  subjectToCourses.foreignKeyDeclaration.constrainReference(onDelete cascade)

  //when a course is deleted, all of the subscriptions will get deleted :
  courseSubscriptions.leftForeignKeyDeclaration.constrainReference(onDelete cascade)

  override def drop(): Unit = {
    Session.cleanupResources()
    super.drop()
  }

  val as: Table[ASTConstructionInterferenceA] = table[ASTConstructionInterferenceA]
  val bs: Table[ASTConstructionInterferenceB] = table[ASTConstructionInterferenceB]

  val aToB: PrimitiveTypeModeForTests.OneToManyRelationImpl[ASTConstructionInterferenceA, ASTConstructionInterferenceB] =
    oneToManyRelation(as, bs).via((a, b) => a.id === b.aId)

  aToB.foreignKeyDeclaration.unConstrainReference
}

abstract class SchoolDb2Tests extends SchemaTester with RunTestsInsideTransaction with QueryTester {
  self: DBConnector =>

  val schema = new SchoolDb2

  import schema._

  def seedDataDef() = new {

    val professeurTournesol: Professor = professors.insert(new Professor("Tournesol"))
    val madProfessor: Professor = professors.insert(new Professor("Mad Professor"))

    val philosophy: Subject = subjects.insert(new Subject("Philosophy", None))
    val chemistry: Subject = subjects.insert(new Subject("Chemistry", None))
    val physics: Subject = subjects.insert(new Subject("Physic", None))
    val computerScience: Subject = subjects.insert(new Subject("Computer Science", None))
    val computationTheory: Subject = subjects.insert(new Subject("Computation Theory", Some(computerScience.id)))

    val chemistryCourse: Course = courses.insert(new Course(chemistry.id))
    val physicsCourse: Course = courses.insert(new Course(physics.id))

    val xiaoJimbao: Student = students.insert(new Student("Xiao", "Jimbao"))
  }

//  def testAll = {
//
//    testInFromSeq
//    testInFromSet
//
//    testIssue68
//
//    val entry = entries.insert(Entry("An entry"))
//    val comment = Comment("A single comment")
//    entry.comments.associate(comment)
//
//    from(entry.comments)(c => where(c.id === comment.id) select(c))
//
//    seedData
//
//    testUpdateWithCompositePK
//
//    testCompositeEquality
//
//    testMany2ManyAssociationFromLeftSide
//    testMany2ManyAssociationsFromRightSide
//
//    testOneToMany
//
//    testUniquenessConstraint
//
//    SchoolDb2.drop
//  }

  test("select using query value") {
    seedDataDef()

    val q: Query[String] =
      from(subjects)(
        s =>
          where(s.name === "Philosophy")
            select &(from(subjects)(s2 => where(s2.name === s.name) select s2.name))
      )

    assertEquals(1, q.toList.length, "Could not find row")
  }

  test("equality using query value") {
    seedDataDef()

    val q: Query[String] =
      from(subjects)(
        s =>
          where(
            s.name === from(subjects)(s2 => where(s2.name === "Philosophy") select s2.name)
          )
            select s.name
      )

    assertEquals(1, q.toList.length, "Could not find row")
  }

  test("associate comment") {
    val entry   = entries.insert(Entry("An entry"))
    val comment = Comment("A single comment")
    entry.comments.associate(comment)

    from(entry.comments)(c => where(c.id === comment.id) select c)
  }

  test("UpdateWithCompositePK") {
    val seedData = seedDataDef()
    import seedData._

    val xiao = { students.lookup(xiaoJimbao.id) }.get

    val courseSubscription = xiao.courses.assign(chemistryCourse)

    courseSubscriptions.insert(courseSubscription)
    courseSubscription.grade = 95.0F
    courseSubscriptions.update(courseSubscription)

    val cs2 = courseSubscriptions.lookup(courseSubscription.id).get

    assertEquals(95.0F, cs2.grade, 'testUpdateWithCompositePK)

    passed('testUpdateWithCompositePK)
  }

  test("Many2ManyAssociationFromLeftSide") {
    val seedData = seedDataDef()
    import seedData._

    assertEquals(0, courseAssignments.Count: Long, 'testMany2ManyAssociationFromLeftSide)

    professeurTournesol.courses.associate(physicsCourse)

    val c1 = professeurTournesol.courses.single: Course

    assertEquals(c1.id, physicsCourse.id, 'testMany2ManyAssociationFromLeftSide)

    val ca = professeurTournesol.courses.associations.single: CourseAssignment

    assertEquals(ca.courseId, physicsCourse.id, 'testMany2ManyAssociationFromLeftSide)

    assertEquals(professeurTournesol.courses.dissociateAll, 1, 'testMany2ManyAssociationFromLeftSide)

    assertEquals(professeurTournesol.courses.dissociateAll, 0, 'testMany2ManyAssociationFromLeftSide)

    assertEquals(0, courseAssignments.Count: Long, 'testMany2ManyAssociationFromLeftSide)

    passed('testMany2ManyAssociationFromLeftSide)
  }

  test("Many2ManyAssociationsFromRightSide") {
    val seedData = seedDataDef()
    import seedData._

    assertEquals(0, courseAssignments.Count: Long, 'testMany2ManyAssociationsFromRightSide)

    physicsCourse.professors.associate(professeurTournesol)

    val profT = physicsCourse.professors.single: Professor

    assertEquals(professeurTournesol.lastName, profT.lastName, 'testMany2ManyAssociationsFromRightSide)

    val ca = professeurTournesol.courses.associations.single: CourseAssignment

    assertEquals(ca.courseId, physicsCourse.id, 'testMany2ManyAssociationsFromRightSide)

    assertEquals(physicsCourse.professors.dissociateAll, 1, 'testMany2ManyAssociationsFromRightSide)

    assertEquals(physicsCourse.professors.dissociateAll, 0, 'testMany2ManyAssociationsFromRightSide)

    assertEquals(0, courseAssignments.Count: Long, 'testMany2ManyAssociationsFromRightSide)

    passed('testMany2ManyAssociationsFromRightSide)
  }

  test("OneToMany") {
    val seedData = seedDataDef()
    import seedData._

    val philosophyCourse10AMWednesday = new Course
    val philosophyCourse2PMWednesday  = new Course
    val philosophyCourse3PMFriday     = new Course

    philosophy.courses.associate(philosophyCourse10AMWednesday)
    philosophy.courses.associate(philosophyCourse2PMWednesday)
    philosophy.courses.associate(philosophyCourse3PMFriday)

    assertEquals(
      philosophy.courses.map(_.id).toSet,
      Set(philosophyCourse10AMWednesday.id, philosophyCourse2PMWednesday.id, philosophyCourse3PMFriday.id),
      'testOneToMany
    )

    assertEquals(philosophyCourse2PMWednesday.subject.single.name, philosophy.name, 'testOneToMany)

    // verify that a reassociation does an update and not an insert :
    val pk1 = philosophyCourse3PMFriday.id

    computationTheory.courses.associate(philosophyCourse3PMFriday)

    assertEquals(pk1, philosophyCourse3PMFriday.id, 'testOneToMany)

    // verify that the reassociation worked, which means that
    // 1) : the set of philosophy.courses was reduced properly
    assertEquals(
      philosophy.courses.map(_.id).toSet,
      Set(philosophyCourse10AMWednesday.id, philosophyCourse2PMWednesday.id),
      'testOneToMany
    )

    // 2) philosophyCourse3PMFriday.subject points to the proper subject
    assertEquals(computationTheory.name, philosophyCourse3PMFriday.subject.single.name, 'testOneToMany)

    passed('testOneToMany)
  }

  test("CompositeEquality") {
    val seedData = seedDataDef()
    import seedData._

    val a = physicsCourse.professors.associate(professeurTournesol)

    val qA = courseAssignments.lookup(compositeKey(a.courseId, a.professorId))

    _existsAndEquals(qA, a)

    val qA2 =
      from(courseAssignments)(
        ca =>
          where(ca.id === (a.courseId, a.professorId))
            select ca
      )

    _existsAndEquals(qA2.headOption, a)

    //println(qA2.statement)

    val qA3 =
      courseAssignments.where(_.id === a.id)

    _existsAndEquals(qA3.headOption, a)

    courseAssignments.delete(compositeKey(a.courseId, a.professorId))

    assertEquals(0L, qA3.Count: Long, 'testCompositeEquality)

    //println(ca2.statement)
    passed('testCompositeEquality)
  }

  private def _existsAndEquals(oca: Option[CourseAssignment], ca: CourseAssignment): Unit = {

    if (oca.isEmpty)
      org.squeryl.internals.Utils.throwError("query returned no rows")

    assertEquals(ca.id, oca.get.id, 'testCompositeEquality)
  }

  test("UniquenessConstraint") {
    val seedData = seedDataDef()
    import seedData._

    assertEquals(0, courseAssignments.Count: Long, 'testUniquenessConstraint)

    physicsCourse.professors.associate(professeurTournesol)

    assertEquals(1, courseAssignments.Count: Long, 'testUniquenessConstraint)

    var exceptionThrown = false

    val s = Session.currentSession

    val sp: Option[Savepoint] =
      if (s.databaseAdapter.failureOfStatementRequiresRollback)
        Some(s.connection.setSavepoint())
      else
        None

    try {
      physicsCourse.professors.associate(professeurTournesol)
    } catch {
      case e: RuntimeException =>
        exceptionThrown = true
        sp.foreach(s.connection.rollback(_))
    }

    if (!exceptionThrown)
      org.squeryl.internals.Utils.throwError('testUniquenessConstraint + " failed, unique constraint violation occured")

    assertEquals(1, courseAssignments.Count: Long, 'testUniquenessConstraint)

    passed('testUniquenessConstraint)
  }

//  test("Issue68"){
//    //https://github.com/squeryl/squeryl/issues#issue/68
//    // Invoking a persisent field during construction causes interference in AST construction
//
//    val a = new ASTConstructionInterferenceA
//    val bs = a.bs
//    val ast = bs.ast.asInstanceOf[QueryExpressionElements]
//
//    val andExp = ast.whereClause.get.asInstanceOf[EqualityExpression]
//
//    assert(andExp.left.isInstanceOf[ConstantExpressionNode[_]], "expected a ConstantExpressionNode[_] in the where clause :\n" + bs.statement)
//
//    bs.deleteAll
//    passed('testIssue68)
//  }

  test("InFromSet") {
    val set = Set("foo", "bar", "baz")
    from(entries)(e => where(e.text.in(set)) select e).toList
    passed('testInFromSet)
  }
  test("InFromSeq") {
    val set = Set("foo", "bar", "baz").toSeq
    from(entries)(e => where(e.text.in(set)) select e).toList
    passed('testInFromSeq)
  }

  test("Inequality with query on right hand side", SingleTestRun) {
    val seedData = seedDataDef()
    import seedData._

    val xiao = students.lookup(xiaoJimbao.id).get

    val courseSubscription = xiao.courses.assign(chemistryCourse)

    courseSubscriptions.insert(courseSubscription)
    courseSubscription.grade = 95.0F
    courseSubscriptions.update(courseSubscription)

    val cs2 = courseSubscriptions.lookup(courseSubscription.id).get

    assertEquals(95.0F, cs2.grade, 'testUpdateWithCompositePK)

    from(courseSubscriptions)(p => compute(avg(p.grade)))

    val belowOrEqualToAvg =
      from(courseSubscriptions)(
        p =>
          where(p.grade lte from(courseSubscriptions)(p => compute(avg(p.grade))))
            select p
      ).toList

    assert(belowOrEqualToAvg.size == 1)

    val belowAvg =
      from(courseSubscriptions)(
        p =>
          where(p.grade lt from(courseSubscriptions)(p => compute(avg(p.grade))))
            select p
      ).toList

    assert(belowAvg.isEmpty)
  }

  test("#73 relations with Option[] on one side of the equality expression blow up") {

    seedDataDef()

    val cs = subjects.where(_.name === "Computer Science").single

    val compTheory = cs.childSubjects.single

    assert(compTheory.name == "Computation Theory")

  }
}
