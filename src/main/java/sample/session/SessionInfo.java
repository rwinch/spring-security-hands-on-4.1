package sample.session;

import java.util.Date;

import org.springframework.session.ExpiringSession;

@SuppressWarnings("serial")
public class SessionInfo extends SessionDetails {
	private String id;
	private Date created;
	private Date lastUpdated;

	public SessionInfo(ExpiringSession session) {
		SessionDetails details = session.getAttribute("SESSION_DETAILS");
		if(details != null) {
			setAccessType(details.getAccessType());
			setLocation(details.getLocation());
		}
		setCreated(new Date(session.getCreationTime()));
		setLastUpdated(new Date(session.getLastAccessedTime()));
		setId(session.getId());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}


}
