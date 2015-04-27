package server.awk;

import java.io.File;

public class MailWrapper {
	private final File mail;
	private boolean deleted = false;
	
	public MailWrapper(File mail) {
		super();
		this.mail = mail;
	}
	
	public File getMail(){
		return mail;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	
	
}
