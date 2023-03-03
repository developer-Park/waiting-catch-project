package team.waitingcatch.app.lineup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LineupController {
	@GetMapping("/seller/lineup")
	public String getLineupPage() {
		return "seller/lineup";
	}
}