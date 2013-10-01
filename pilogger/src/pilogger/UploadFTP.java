package pilogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.file.Path;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class UploadFTP {
	private static final FTPClient ftp = new FTPClient();
	private static final String HOSTNAME = "ftpperso.free.fr";
	
	public static synchronized void store(String localFileName, String localPath) throws SocketException, IOException {
		
		ftp.connect(HOSTNAME);
		if (! FTPReply.isPositiveCompletion(  ftp.getReplyCode() )) {
			ftp.disconnect();
			return;
		}
		
		ftp.login("muth.inc", "********");
		ftp.setFileType(FTP.BINARY_FILE_TYPE);
		ftp.enterLocalPassiveMode();
		
		InputStream input = new FileInputStream(new File(localPath+localFileName));
		ftp.storeFile("pilogger/" + localFileName, input);
		ftp.disconnect();
	}
	
public static synchronized void store(Path localFilePath) throws SocketException, IOException {
		
		ftp.connect(HOSTNAME);
		if (! FTPReply.isPositiveCompletion(  ftp.getReplyCode() )) {
			ftp.disconnect();
			return;
		}
		
		ftp.login("muth.inc", "gx88sodw");
		ftp.setFileType(FTP.BINARY_FILE_TYPE);
		ftp.enterLocalPassiveMode();
		
		InputStream input = new FileInputStream(localFilePath.toFile());
		ftp.storeFile("pilogger/" + localFilePath.getFileName(), input);
		ftp.disconnect();
	}

}
