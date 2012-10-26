package org.cloudcoder.app.client.rpc;

import com.google.gwt.core.client.GWT;

public abstract class RPC {
	public static final ConfigurationSettingServiceAsync configurationSettingService = GWT.create(ConfigurationSettingService.class);
	public static final LoginServiceAsync loginService = GWT.create(LoginService.class);
	public static final GetCoursesAndProblemsServiceAsync getCoursesAndProblemsService = GWT.create(GetCoursesAndProblemsService.class);
	public static final EditCodeServiceAsync editCodeService = GWT.create(EditCodeService.class);
	public static final SubmitServiceAsync submitService = GWT.create(SubmitService.class);
	public static final UserServiceAsync usersService= GWT.create(UserService.class);
}
