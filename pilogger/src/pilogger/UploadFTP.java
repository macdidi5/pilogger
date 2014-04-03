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
	private static final String HOSTNAME = "xx.xx.xx";

	public static synchronized void store(Path localFilePath) {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect(HOSTNAME);
			
			if (! FTPReply.isPositiveCompletion(  ftp.getReplyCode() )) {
				ftp.disconnect();
				return;
			}

			ftp.login("xxx", "yyy");
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();

			InputStream input = new FileInputStream(localFilePath.toFile());
			ftp.storeFile("pilogger/" + localFilePath.getFileName(), input);
			ftp.logout();
			ftp.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
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
