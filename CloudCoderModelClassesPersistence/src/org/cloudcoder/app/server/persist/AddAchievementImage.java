package org.cloudcoder.app.server.persist;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.AchievementImage;

public class AddAchievementImage {
	public static void main(String[] args) throws IOException, SQLException {
		ConfigurationUtil.configureLog4j();
		
		Properties config = DBUtil.getConfigProperties();
		
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Image file: ");
		String fileName = keyboard.nextLine();
		
		byte[] data = readAllBytes(fileName);
		
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

	private static byte[] readAllBytes(String fileName) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileInputStream in = null;
		try {
			in = new FileInputStream(fileName);
			IOUtils.copy(in, out);
			return out.toByteArray();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
