package com.geomeet.api.adapter.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResponseUtilTest {

    @Test
    void shouldCreateOkResponse() {
        // Given
        String body = "test body";

        // When
        ResponseEntity<String> response = ResponseUtil.ok(body);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(body, response.getBody());
    }

    @Test
    void shouldCreateOkResponseWithNullBody() {
        // When
        ResponseEntity<String> response = ResponseUtil.ok(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldCreateCreatedResponse() {
        // Given
        String body = "created resource";

        // When
        ResponseEntity<String> response = ResponseUtil.created(body);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(body, response.getBody());
    }

    @Test
    void shouldCreateCreatedResponseWithNullBody() {
        // When
        ResponseEntity<String> response = ResponseUtil.created(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldCreateResponseWithCustomStatus() {
        // Given
        String body = "custom body";
        HttpStatus status = HttpStatus.ACCEPTED;

        // When
        ResponseEntity<String> response = ResponseUtil.status(status, body);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(body, response.getBody());
    }

    @Test
    void shouldCreateResponseWithNotFoundStatus() {
        // Given
        String body = "not found";
        HttpStatus status = HttpStatus.NOT_FOUND;

        // When
        ResponseEntity<String> response = ResponseUtil.status(status, body);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(body, response.getBody());
    }

    @Test
    void shouldCreateNoContentResponse() {
        // When
        ResponseEntity<Object> response = ResponseUtil.noContent();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldCreateResponseWithDifferentTypes() {
        // Test with Integer
        ResponseEntity<Integer> intResponse = ResponseUtil.ok(42);
        assertEquals(42, intResponse.getBody());

        // Test with Boolean
        ResponseEntity<Boolean> boolResponse = ResponseUtil.ok(true);
        assertEquals(true, boolResponse.getBody());

        // Test with custom object
        TestObject testObj = new TestObject("test");
        ResponseEntity<TestObject> objResponse = ResponseUtil.ok(testObj);
        assertEquals(testObj, objResponse.getBody());
    }

    // Helper class for testing
    private static class TestObject {
        private final String value;

        TestObject(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestObject that = (TestObject) o;
            return value != null ? value.equals(that.value) : that.value == null;
        }
    }
}

