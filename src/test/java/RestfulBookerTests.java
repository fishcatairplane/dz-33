import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RestfulBookerTests {

    private static final String BASE_URL = "http://restful-booker.herokuapp.com/";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void createBookingTest() {
        given()
                .header("Content-Type", "application/json")
                .body("{\"firstname\": \"John\", \"lastname\": \"Doe\", \"totalprice\": 100, \"depositpaid\": true, \"bookingdates\": {\"checkin\": \"2023-07-07\", \"checkout\": \"2023-07-10\"}, \"additionalneeds\": \"Breakfast\"}")
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .body("bookingid", notNullValue())
                .body("booking.firstname", equalTo("John"))
                .body("booking.lastname", equalTo("Doe"))
                .body("booking.totalprice", equalTo(100))
                .body("booking.depositpaid", equalTo(true))
                .body("booking.bookingdates.checkin", equalTo("2023-07-07"))
                .body("booking.bookingdates.checkout", equalTo("2023-07-10"))
                .body("booking.additionalneeds", equalTo("Breakfast"));
    }

    @Test
    public void getAllBookingsTest() {
        given()
                .get("/booking")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

}
