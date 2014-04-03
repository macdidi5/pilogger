package pilogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.file.Path;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class UploadFTP {
	private static final String HOSTNAME = "xxx";

	public static synchronized boolean store(Path localFilePath) {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect(HOSTNAME);
			
			if (! FTPReply.isPositiveCompletion(  ftp.getReplyCode() )) {
				ftp.disconnect();
				return false;
			}

			ftp.login("xxx", "xxx");
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();

			InputStream input = new FileInputStream(localFilePath.toFile());
			ftp.storeFile("pilogger/" + localFilePath.getFileName(), input);
			ftp.logout();
			ftp.disconnect();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					// do nothing
				}
			}
		}

	}

}
