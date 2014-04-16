package edu.gatech.cs7641.assignment3;

import static org.junit.Assert.*;

import org.junit.Test;

public class T3BoardTest {

	@Test
	public void test1() {
		String test1 = "OOX"+"OXX"+"X O";
		T3Board b = new T3Board(test1);
		assertTrue(b.isTerminal());
		assertTrue(b.hasWon(T3Board.X));
	}
	
	@Test
	public void test2() {
		String test1 = " OO"+"OOX"+"XXX";
		T3Board b = new T3Board(test1);
		assertTrue(b.isTerminal());
		assertTrue(b.hasWon(T3Board.X));
	}

	@Test
	public void test_rotateClockwise() {
		String test = "O X"+"   "+"OOX";
		T3Board b = new T3Board(test);
		T3Board c = new T3Board(test);
		c.rotateClockwise();
		assertNotEquals(b.toString(),c.toString());
		c.rotateClockwise();
		assertNotEquals(b.toString(),c.toString());
		c.rotateClockwise();
		assertNotEquals(b.toString(),c.toString());
		c.rotateClockwise();
		assertEquals(b.toString(),c.toString());
	}
	
	@Test
	public void test_reflectAboutDiagonal() {
		String test = "O X"+"   "+"OOX";
		T3Board b = new T3Board(test);
		T3Board c = new T3Board(test);
		c.reflectAboutDiagonal();
		assertNotEquals(b.toString(),c.toString());
		c.reflectAboutDiagonal();
		assertEquals(b.toString(),c.toString());
	}
	
	@Test
	public void test_reflectAboutOtherDiagonal() {
		String test = "O X"+"   "+"OOX";
		T3Board b = new T3Board(test);
		T3Board c = new T3Board(test);
		c.reflectAboutOtherDiagonal();
		assertNotEquals(b.toString(),c.toString());
		c.reflectAboutOtherDiagonal();
		assertEquals(b.toString(),c.toString());
	}
}
