package org.springframework.samples.petclinic;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class PetClinicIntegrationTests {

	@Test
	void ownerDetails() {
		given()
			.when().get("/owners/1")
			.then().statusCode(200);
	}

	@Test
	void ownerList() {
		given()
			.queryParam("lastName", "")
			.when().get("/owners")
			.then().statusCode(200);
	}

}
