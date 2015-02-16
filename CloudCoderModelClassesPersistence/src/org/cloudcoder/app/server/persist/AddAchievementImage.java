package org.cloudcoder.app.server.persist;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.AchievementImage;

public class AddAchievementImage {
	public static void main(String[] args) throws IOException, SQLException {
		Properties config = DBUtil.getConfigProperties();
		
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Image file: ");
		String fileName = keyboard.nextLine();
		
		byte[] data = Files.readAllBytes(new File(fileName).toPath());
		
		AchievementImage img = new AchievementImage();
		img.setImageArr(data);
		
		Connection conn = null;
		try{
			conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
			DBUtil.storeModelObject(conn, img);
			System.out.printf("Stored achievement image with id=%d\n", img.getId());
		} finally {
			DBUtil.closeQuietly(conn);
		}
	}
}
