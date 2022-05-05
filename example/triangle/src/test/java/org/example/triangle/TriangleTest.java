package org.example.triangle;

import org.junit.Test;

import static org.junit.Assert.*;

public class TriangleTest {

    @Test
    public void getTriangleType() {
        final Triangle triangle = new Triangle();
        assertEquals("Isosceles", triangle.getTriangleType(1, 5, 5));
        assertEquals("Isosceles", triangle.getTriangleType(2, 5, 5));
        assertEquals("Isosceles", triangle.getTriangleType(9, 5, 5));
        assertNull(triangle.getTriangleType(10, 5, 5));
        assertEquals("Isosceles", triangle.getTriangleType(5, 1, 5));
        assertEquals("Isosceles", triangle.getTriangleType(5, 2, 5));
        assertEquals("Isosceles", triangle.getTriangleType(5, 9, 5));
        assertNull(triangle.getTriangleType(5, 10, 5));
        assertEquals("Equilatral", triangle.getTriangleType(5, 5, 5));
        assertEquals("Scalene", triangle.getTriangleType(4, 2, 3));
    }
}