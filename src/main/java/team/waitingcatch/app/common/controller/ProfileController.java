package team.waitingcatch.app.common.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProfileController {
	private final Environment env;

	@GetMapping("/profile")
	public String getProfile() {
		List<String> profiles = Arrays.asList(env.getActiveProfiles());
		List<String> prodProfiles = Arrays.asList("prod1", "prod2");
		String defaultProfile = profiles.isEmpty() ? "default" : profiles.get(0);

		return profiles.stream().filter(prodProfiles::contains).findAny().orElse(defaultProfile);
	}
}