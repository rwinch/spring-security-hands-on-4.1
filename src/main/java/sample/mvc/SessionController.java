/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package sample.mvc;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.session.ExpiringSession;
import org.springframework.session.FindByPrincipalNameSessionRepository;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sample.data.User;
import sample.security.CurrentUser;
import sample.session.SessionInfo;

/**
 * Controller for sending the user to the login view.
 *
 * @author Rob Winch
 *
 */
@RestController
public class SessionController {
	@Autowired
	FindByPrincipalNameSessionRepository<? extends ExpiringSession> sessions;


	/**
	 * @param assembler
	 * @param currentUser
	 * @return
	 */
	@RequestMapping(value = "/sessions/", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
	public ResponseEntity<?> index(PersistentEntityResourceAssembler assembler, @CurrentUser User currentUser) {
		Collection<? extends ExpiringSession> usersSessions =
				sessions.findByPrincipalName(currentUser.getEmail()).values();

		List<SessionInfo> info = new ArrayList<SessionInfo>(usersSessions.size());
		for(ExpiringSession session : usersSessions) {
			info.add(new SessionInfo(session));
		}

		return new ResponseEntity<List<SessionInfo>>(info, HttpStatus.OK);
	}

	@RequestMapping(value = "/sessions/", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
	public HttpStatus removeSession(Principal principal, @RequestHeader("x-auth-token") String sessionIdToDelete) {
		Set<String> usersSessionIds = sessions.findByPrincipalName(principal.getName()).keySet();
		if(usersSessionIds.contains(sessionIdToDelete)) {
			sessions.delete(sessionIdToDelete);
		}

		return HttpStatus.NO_CONTENT;
	}

}