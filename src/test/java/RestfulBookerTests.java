import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Random;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class RestfulBookerTests {
    public static String TOKEN_VALUE;
    public static final String TOKEN = "token";

    @BeforeMethod
    public void setUp() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .build();

        JSONObject body = new JSONObject();
        body.put("username", "admin");
        body.put("password", "password123");

        Response response = RestAssured.given()
                .body(body.toString())
                .post("/auth");
        TOKEN_VALUE = response.then().extract().jsonPath().get(TOKEN);
    }

    @Test(description ="Getting all booking IDs")
    public void getAllBookingIdsTest(){
        Response allBookingIds = RestAssured.given().log().all().get("/booking");
        allBookingIds.then().statusCode(200);
        allBookingIds.prettyPrint();
        allBookingIds.then().assertThat().body(matchesJsonSchemaInClasspath("AllBookingSchema.json"));
    }

    @Test(description = "Getting a booking by ID")
    public void getBookingByIdTest() {
        Response bookingId = RestAssured.given()
                .log().all()
                .pathParam("id", 1)
                .get("/booking/{id}");
        bookingId.prettyPrint();
        bookingId.then().body("firstname", equalTo("Mark"));
        bookingId.then().statusCode(200);
        bookingId.then().body("bookingdates.checkin", greaterThanOrEqualTo("2018-01-01"));
        bookingId.jsonPath().get("totalprice");
    }

    @Test(description = "Creating a booking")
    public void createNewBookingTest() {
        String firstNameExpected = "Mira";
        Integer totalPriceExpected = 1000;
        Boolean depositPaidExpected = false;

        JSONObject bookingBody = new JSONObject();
        bookingBody.put("firstname", "Mira");
        bookingBody.put("lastname", "Belle");
        bookingBody.put("totalprice", 1000);
        bookingBody.put("depositpaid", false);

        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", "2023-07-05");
        bookingDates.put("checkout", "2023-07-10");
        bookingBody.put("bookingdates", bookingDates);

        bookingBody.put("additionalneeds", "TestNeeds");

        Response newBooking = RestAssured.given()
                .body(bookingBody.toString())
                .post("/booking");
        newBooking.prettyPrint();
        String firstNameResponse = newBooking.jsonPath().get("booking.firstname");
        Integer totalPriceResponse = newBooking.jsonPath().get("booking.totalprice");
        Boolean depositPaidResponse = newBooking.jsonPath().get("booking.depositpaid");
        newBooking.then().statusCode(200);
        newBooking.then().body("bookingid", notNullValue());
        Assert.assertEquals(firstNameResponse, firstNameExpected, "The First name is wrong");
        Assert.assertEquals(totalPriceResponse, totalPriceExpected, "The Total price is wrong");
        Assert.assertEquals(depositPaidResponse, depositPaidExpected, "The Deposit paid is wrong");
    }

    @Test(description = "Partially updating a booking")
    public void partialUpdateBookingTest() {
        JSONObject body = new JSONObject();
        body.put("totalprice", 500);

        Response updatedBooking = RestAssured.given()
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .cookie(TOKEN, TOKEN_VALUE)
                .body(body.toString())
                .patch("/booking/{id}", 718);
        updatedBooking.prettyPrint();
        updatedBooking.then().statusCode(200);
    }

    @Test(description = "Updating a booking")
    public void updateBookingTest() {
        String lastNameExpected = "TestLastNameModified";

        JSONObject bookingBody = new JSONObject();
        bookingBody.put("firstname", "TestNameModified");
        bookingBody.put("lastname", "TestLastNameModified");
        bookingBody.put("totalprice", 1000);
        bookingBody.put("depositpaid", true);

        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", "2023-07-01");
        bookingDates.put("checkout", "2023-07-05");
        bookingBody.put("bookingdates", bookingDates);

        bookingBody.put("additionalneeds", "TestNeedsModified");

        Response updatedBooking = RestAssured.given()
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .cookie(TOKEN, TOKEN_VALUE)
                .body(bookingBody.toString())
                .put("/booking/{id}", 100);
        updatedBooking.prettyPrint();
        String lastNameResponse = updatedBooking.jsonPath().get("lastname");
        updatedBooking.then().statusCode(200);
        Assert.assertEquals(lastNameResponse, lastNameExpected, "The Last name is wrong");
    }

    @Test(description = "Deleting a booking")
    public void deleteBookingTest() {
        Random rand = new Random();
        int upperbound = 2000;
        int int_random = rand.nextInt(upperbound);

        Response deleteBooking = RestAssured.given()
                .cookie(TOKEN, TOKEN_VALUE)
                .delete("/booking/{id}", int_random);
        deleteBooking.prettyPrint();
        deleteBooking.then().statusCode(201);
    }
}
