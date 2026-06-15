package org.springframework.samples.petclinic.system;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CrashResourceTests {

	@Test
	void shouldReturnErrorOnCrash() {
		given()
			.when().get("/oups")
			.then().statusCode(500);
	}

}
