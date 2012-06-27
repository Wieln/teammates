package teammates.testing.testcases;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.api.Common;
import teammates.datatransfer.CourseData;
import teammates.datatransfer.DataBundle;
import teammates.datatransfer.StudentData;
import teammates.jsp.Helper;
import teammates.testing.config.Config;
import teammates.testing.lib.BackDoor;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;
import teammates.testing.testcases.BaseTestCase;

public class AccessControlUiTest extends BaseTestCase {
	
	private static BrowserInstance bi;
	private static String appUrl = Config.inst().TEAMMATES_URL;
	
	@BeforeClass
	public static void classSetup(){
		printTestClassHeader();
		bi = BrowserInstancePool.getBrowserInstance();
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserInstancePool.release(bi);
		printTestClassFooter();
	}
	
	@Test
	public void testUserNotLoggedIn() throws Exception{
		
		bi.logout();
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/login.html");
		
		______TS("student");
		
		verifyRedirectToLogin(Common.PAGE_STUDENT_HOME);
		verifyRedirectToLogin(Common.PAGE_STUDENT_JOIN_COURSE);
		verifyRedirectToLogin(Common.PAGE_STUDENT_COURSE_DETAILS);
		verifyRedirectToLogin(Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT);
		verifyRedirectToLogin(Common.PAGE_STUDENT_EVAL_RESULTS);
		
		______TS("coord");
		
		verifyRedirectToLogin(Common.PAGE_COORD_HOME);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_DELETE);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_DETAILS);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_ENROLL);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_REMIND);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_STUDENT_DELETE);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_STUDENT_DETAILS);
		verifyRedirectToLogin(Common.PAGE_COORD_COURSE_STUDENT_EDIT);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_EDIT);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_DELETE);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_REMIND);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_RESULTS);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_PUBLISH);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_UNPUBLISH);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_SUBMISSION_VIEW);
		verifyRedirectToLogin(Common.PAGE_COORD_EVAL_SUBMISSION_EDIT);
		
	}
	
	@Test
	public void testUserNotRegistered() throws Exception{
		
		______TS("student");
		
		String unregUsername = Config.inst().TEST_UNREG_ACCOUNT;
		String unregPassword = Config.inst().TEST_UNREG_PASSWORD;
		bi.logout();
		bi.loginStudent(unregUsername, unregPassword);
		
		verifyRedirectToWelcomeStrangerPage(Common.PAGE_STUDENT_HOME, unregUsername);
		verifyRedirectToWelcomeStrangerPage(Common.PAGE_STUDENT_JOIN_COURSE, unregUsername);
		
		verifyRedirectToNotAuthorized(Common.PAGE_STUDENT_COURSE_DETAILS);
		verifyRedirectToNotAuthorized(Common.PAGE_STUDENT_EVAL_SUBMISSION_EDIT);
		verifyRedirectToNotAuthorized(Common.PAGE_STUDENT_EVAL_RESULTS);
		
		______TS("coord");
		
		bi.logout();
		bi.loginCoord(unregUsername, unregPassword);
		
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_HOME);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_DELETE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_DETAILS);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_ENROLL);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_REMIND);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_DELETE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_DETAILS);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_COURSE_STUDENT_EDIT);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_EDIT);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_DELETE);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_REMIND);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_RESULTS);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_PUBLISH);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_UNPUBLISH);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_SUBMISSION_VIEW);
		verifyRedirectToNotAuthorized(Common.PAGE_COORD_EVAL_SUBMISSION_EDIT);
		
	}
	
	@Test
	public void testStudentAccessControl() throws Exception{
		
		
		
		String studentUsername = Config.inst().TEST_STUDENT_ACCOUNT;
		String studentPassword = Config.inst().TEST_STUDENT_PASSWORD;
		
		startRecordingTimeForDataImport();
		DataBundle dataBundle = getTypicalDataBundle();
		StudentData student = dataBundle.students.get("student1InCourse1");
		//assign the test user id to an existing student
		student.id = studentUsername;
		String backDoorOperationStatus = BackDoor.restoreNewDataBundle(Common.getTeammatesGson().toJson(dataBundle));
		assertEquals(Common.BACKEND_STATUS_SUCCESS, backDoorOperationStatus);
		reportTimeForDataImport();
		
		bi.logout();
		bi.loginStudent(studentUsername, studentPassword);
		
		verifyPageContains(Common.PAGE_STUDENT_HOME, studentUsername+"{*}Student Home{*}View Team");
		verifyPageContains(Common.PAGE_STUDENT_JOIN_COURSE, studentUsername+"{*}Student Home{*}View Team");
		
		______TS("student view details of a student's own course");
		
		String link = Common.PAGE_STUDENT_COURSE_DETAILS;
		String idOfOwnCourse = "idOfCourse1OfCoord1";
		link = Helper.addParam(link, Common.PARAM_COURSE_ID, idOfOwnCourse);
		verifyPageContains(link, studentUsername+"{*}Team Details for "+idOfOwnCourse);
		
		______TS("student tries to view details of a course she is not registered for");

		link = Common.PAGE_STUDENT_COURSE_DETAILS;
		CourseData otherCourse = dataBundle.courses.get("course1OfCoord2");
		link = Helper.addParam(link, Common.PARAM_COURSE_ID, otherCourse.id);
		verifyRedirectToNotAuthorized(link);
		
		______TS("student tries to view course details while masquerading as a student in that course");
		
		StudentData otherStudent = dataBundle.students.get("student1InCourse2");
		//ensure other student belong to other course
		assertEquals(otherStudent.course, otherCourse.id); 
		link = Helper.addParam(link, Common.PARAM_USER_ID , otherStudent.id);
		verifyRedirectToNotAuthorized(link);
		
	}

	private void verifyRedirectToWelcomeStrangerPage(String path, String unregUsername) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		//A simple regex check is enough because we do full HTML tests elsewhere
		assertContainsRegex("{*}"+unregUsername+"{*}Welcome stranger{*}", bi.getCurrentPageSource());
	}

	private void verifyRedirectToNotAuthorized(String path) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		assertContains("You are not authorized to view this page.", bi.getCurrentPageSource());
	}
	
	private void verifyPageContains(String path, String targetText) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		assertContainsRegex(targetText, bi.getCurrentPageSource());
	}

	private void verifyRedirectToLogin(String path) {
		printUrl(appUrl+path);
		bi.goToUrl(appUrl+path);
		assertTrue(bi.isLocalLoginPage()||bi.isGoogleLoginPage());
	}

	private void printUrl(String url) {
		print("   "+url);
	}

}
