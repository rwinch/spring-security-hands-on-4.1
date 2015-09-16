(function($, ko) {

    function CsrfToken(data) {
        this.headerName = ko.observable(data.headerName);
        this.token = ko.observable(data.token);
    }

    function User(data) {
        this.href = ko.observable(data._links ? data._links.self.href : '');
        this.email = ko.observable(data.email);
        this.password = ko.observable(data.password);
    }

    function Message(data) {
        this.href = ko.observable(data._links.self.href);
        this.text = ko.observable(data.text);
        this.summary = ko.observable(data.summary);
        this.created = ko.observable(new Date(data.created));
        this.to = ko.observable(data.to);
        this.from = ko.observable(data.from);
    }

    function SessionInfo(data) {
    	var self = this;
    	this.accessType = ko.observable(data.accessType);
    	this.created = ko.observable(moment(data.created).fromNow());
    	this.id = ko.observable(data.id);
    	this.location = ko.observable(data.location);
    	this.lastUpdated = ko.observable(moment(data.lastUpdated).fromNow());
        this.information = ko.observable(data.accessType);
        this.idSuffix = ko.computed(function() {
        	return self.id().substring(0,4);
        });
    }

    function ComposeMessage(data) {
        this.text = ko.observable(data.text);
        this.summary = ko.observable(data.summary);
        this.from = ko.observable(data.from);
        this.toEmail = ko.observable('');
        this.to = ko.observable('');
    }

    function MessageListViewModel() {
        this.messages = ko.observableArray([]);
        this.chosenMessageData = ko.observable();
        this.inbox = ko.observable();
        this.sent = ko.observable();
        this.compose = ko.observable();
        this.errors = ko.observableArray([]);
        this.user = ko.observable(new User({}));
        this.login = ko.observable();
        this.sessions = ko.observable();
        this.csrf = ko.observable(new CsrfToken({}));

        var self = this;

        self.goToMessage = function(message) {
            self.clearPages();

            $.getJSON(message.href()).then(function(data) {
                self.chosenMessageData(new Message(data));
            });
        };

        self.goToCompose = function() {
            self.clearPages();
            self.compose(new ComposeMessage({
                from:self.user().href(),
                toEmail:'rob@example.com'
            }));
        };

        self.goToInbox = function() {
            return self.goToMailbox('messages/search/inbox', self.inbox);
        };

        self.goToSent = function() {
            return self.goToMailbox('messages/search/sent', self.sent);
        };

        self.goToMailbox = function(mailboxUrl, mailbox) {
            return $.getJSON(mailboxUrl).then(function(allData) {
                self.clearPages();

                var embedded = allData._embedded;
                var mappedMessages = !embedded ? []
                    : embedded.messages.map(function (item) {
                        return new Message(item);
                    });

                self.messages(mappedMessages);
                mailbox(mappedMessages);
            });
        };

        self.goToLogin = function() {
            self.clearPages();
            self.user(new User({}));
            self.login(new User({email:'rob@example.com',password:'password'}));
        };

        self.goToSessions = function() {
            self.clearPages();
            self.getAccountActivity();
        };

        self.clearPages = function() {
            self.sessions(null);
            self.login(null);
            self.inbox(null);
            self.sent(null);
            self.compose(null);
            self.chosenMessageData(null);
        };

        self.save = function() {
            $.ajax('users/search/findByEmail', {
                data: { email: self.compose().toEmail() },
                type: 'get', contentType: 'application/json'
            }).then(function(result, statusText, xhr) {
                if(xhr.status != 200) {
                    return new $.Deferred().reject(new Error('Invalid user'));
                }

                self.compose().to(result._links.self.href);
                return $.ajax('messages', {
                    data: ko.toJSON(self.compose),
                    type: 'post', contentType: 'application/json'
                });
            }).then(self.goToInbox, function(e) {
                self.errors([e.message]);
            });
        };

        self.deleteMessageFromInbox = function(message) {
            self.deleteMessage(message).then(self.goToInbox);
        };

        self.deleteMessageFromSent = function(message) {
            self.deleteMessage(message).then(self.goToSent);
        };

        self.deleteMessage = function(message) {
            return $.ajax(message.href(), { type: 'delete', contentType: 'application/json'});
        };

        self.deleteSession = function(session) {
        	return $.ajax('./sessions/', { type: 'delete', dataType: 'json', headers : { 'x-auth-token' : session.id()}}).then(function() {
        		self.goToSessions();
        	});
        }

        self.performLogin = function() {
            var login = self.login();
            return self.getCurrentUser(login.email(),login.password());
        };

        self.performLogout = function() {
            return $.post('logout').then(function() {
                return self.getCurrentUser();
            });
        };

        self.getCurrentUser = function(username, password) {
            self.user(new User({}));

            var loginAttempt = username != null;
            var additionalHeaders = loginAttempt ? {
                'Authorization': 'Basic ' + btoa(username + ':' + password)
            } : {};
            var errorHandler = loginAttempt ? function(e) {
                if(e.status == 401) {
                    self.errors.push('Invalid username/password');
                }
            } : function(e) {};

            return $.ajax('users/search/self', {
                headers: additionalHeaders,
                type: 'get', contentType: 'application/json',
                error : errorHandler
            }).then(function(result) {
                self.csrf(new CsrfToken({}));
                self.user(new User(result));
                return $.when(self.goToInbox(), self.getCsrf());
            });
        };

        self.getCsrf = function() {
            return $.get('csrf', function(data) {
                self.csrf(new CsrfToken(data));
            });
        };

        self.getAccountActivity = function() {
            $.get('sessions/', function(data) {
                var sessionInfos = data.map(function (item) {
                    return new SessionInfo(item);
                });
                self.sessions(sessionInfos);
            });
        };

    }

    $(function () {
        var messageModel = new MessageListViewModel();
        initAuthInterceptors(messageModel);

        messageModel.getCurrentUser();
        ko.applyBindings(messageModel);
    });

    function initAuthInterceptors (messageModel) {
        $(document).ajaxSend(function (e, xhr /*, options */) {
            messageModel.errors.removeAll();
            xhr.setRequestHeader('Content-type', 'application/json');
            xhr.setRequestHeader('Accept', 'application/json');

            var header = messageModel.csrf().headerName();
            var token = messageModel.csrf().token();
            if (token) {
                xhr.setRequestHeader(header, token);
            }
        });

        $(document).ajaxError(function (event, jqxhr, settings /*, exception */) {
            if (jqxhr.status == 401) {
                messageModel.goToLogin();
            } else if (jqxhr.status == 400) {
                var errors = JSON.parse(jqxhr.responseText).errors;
                errors.forEach(function(error) {
                    messageModel.errors.push(error.message);
                });
            } else {
                alert('Error processing ' + settings.url);
            }
        });
    }

}($, ko));


