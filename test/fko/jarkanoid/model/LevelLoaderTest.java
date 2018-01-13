package fko.jarkanoid.model;


import fko.jarkanoid.model.exceptions.LevelLoaderFormatException;
import fko.jarkanoid.model.exceptions.LevelLoaderIOException;
import fko.jarkanoid.model.exceptions.LevelLoaderNoLevelFilesException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class LevelLoaderTest {

	private String folderTest1 = "/levelsTest/test1/";
	private String folderTest2 = "/levelsTest/test2/";
	private String folderEmpty = "/levelsTest/test1/empty/";
	private String folderProd = "/levels/";
	private String preFix = "Level-";
	private String fileType = ".txt";

	@BeforeAll
	public static void setUp() throws Exception {
	}

	@Test
	public void testLevelLoader() {
		LevelLoader ll =  LevelLoader.getNewInstanceForUnitTest(folderProd, preFix, fileType);
		assertNotNull(ll);
	}

	@Test
	public void testGetLevelFiles() throws Exception {
		LevelLoader ll =  LevelLoader.getNewInstanceForUnitTest(folderProd, preFix, fileType);
		assertNotNull(ll);

		// folder not found
		Exception e = assertThrows(
				LevelLoaderIOException.class, 
				() -> ll.getLevelFiles("unknown"));
		System.out.println(e);

		// file not found
		e = assertThrows(
				LevelLoaderIOException.class, 
				() -> ll.getLinesFromLevelFile(folderTest1, "unknown"));
		System.out.println(e);

		// file found
		List<String> list = ll.getLinesFromLevelFile(folderTest1, "Level-1.txt");
		assertNotNull(list);
		assertTrue(list.size() > 1);

		// process without error
		ll.processLinesFromLevel(folderTest1+"Level-1.txt", list);

		// file found
		List<String> l2 = ll.getLinesFromLevelFile(folderTest1, "Level-2.txt");
		assertNotNull(l2);

		// process without error / only 17 lines
		e = assertThrows(
				LevelLoaderFormatException.class, 
				() -> ll.processLinesFromLevel(folderTest1+"Level-2.txt", l2));
		System.out.println(e);

		// file found
		List<String> l3 = ll.getLinesFromLevelFile(folderTest1, "Level-3.txt");
		assertNotNull(l3);

		// process without error / 19 lines
		e = assertThrows(
				LevelLoaderFormatException.class, 
				() -> ll.processLinesFromLevel(folderTest1+"Level-3.txt", l3));
		System.out.println(e);

		// file found
		List<String> l4 = ll.getLinesFromLevelFile(folderTest1, "Level-4.txt");
		assertNotNull(l4);

		// process without error / 18 lines / one bad line
		e = assertThrows(
				LevelLoaderFormatException.class, 
				() -> ll.processLinesFromLevel(folderTest1+"Level-4.txt", l4));
		System.out.println(e);

		// file found
		List<String> l5 = ll.getLinesFromLevelFile(folderTest2, "Level-1.txt");
		assertNotNull(l5);

		// process without error / 18 lines / one bad item
		e = assertThrows(
				LevelLoaderFormatException.class, 
				() -> ll.processLinesFromLevel(folderTest2+"Level-1.txt", l5));
		System.out.println(e);
	}

	@Test
	public void testItemToBrick() throws Exception {

		assertEquals(new Brick(BrickType.BLUE, PowerPillType.CATCH),
				LevelLoader.itemToBrick("BLCA"));

		assertEquals(new Brick(BrickType.GREY, PowerPillType.LASER),
				LevelLoader.itemToBrick("GYLA"));

		assertEquals(new Brick(BrickType.GOLD, PowerPillType.NONE),
				LevelLoader.itemToBrick("GONO"));

	}

	@Test
	public void testGetLevel() throws Exception {
		LevelLoader ll =  LevelLoader.getNewInstanceForUnitTest(folderProd, preFix, fileType);
		assertNotNull(ll);
		for (int i=1; i<4; i++) {
			System.out.println("Level: "+i);
			Brick[][] bm = ll.getLevel(i);
			for (int row=0; row<bm.length; row++) {
				for (int col=0; col<bm[row].length; col++) {
					System.out.print((bm[row][col] == null ? "----" : bm[row][col].toToken())+" ");
				}
				System.out.println();
			}
		}
	}

	@Test
	public void testInitialize() throws Exception {
		LevelLoader ll =  LevelLoader.getNewInstanceForUnitTest(folderProd, preFix, fileType);
		assertNotNull(ll);
		
		// folder has other files
		Exception e = assertThrows(
				LevelLoaderFormatException.class, 
				() -> ll.initialize(folderTest1));
		System.out.println(e); 
		
		// folder empty, files not found
		e = assertThrows(
				LevelLoaderNoLevelFilesException.class, 
				() -> ll.initialize(folderEmpty));
		System.out.println(e);

		
	}

}
