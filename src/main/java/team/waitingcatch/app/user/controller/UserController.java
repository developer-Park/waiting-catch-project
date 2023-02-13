package team.waitingcatch.app.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.waitingcatch.app.user.dto.CustomerResponse;
import team.waitingcatch.app.user.dto.GetCustomerByIdAndRoleServiceRequest;
import team.waitingcatch.app.user.dto.UpdateUserControllerRequest;
import team.waitingcatch.app.user.dto.UpdateUserServiceRequest;
import team.waitingcatch.app.user.dto.UserCreateControllerRequest;
import team.waitingcatch.app.user.dto.UserCreateServiceRequest;
import team.waitingcatch.app.user.enums.UserRoleEnum;
import team.waitingcatch.app.user.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	// customer
	@PostMapping("/customer/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public void createCustomer(@RequestBody UserCreateControllerRequest controllerRequest) {
		_createUserService(UserRoleEnum.USER, controllerRequest);
	}

	// seller
	@PutMapping("/seller/info")
	public void updateSellerInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody
	UpdateUserControllerRequest controllerRequest) {
		UpdateUserServiceRequest serviceRequest = new UpdateUserServiceRequest(controllerRequest.getName(),
			controllerRequest.getEmail(), userDetails.getUsername(), controllerRequest.getNickName(),
			controllerRequest.getPhoneNumber());
		userService.updateUser(serviceRequest);
	}

	// admin
	@GetMapping("/admin/customers")
	public List<CustomerResponse> getCustomers() {
		return userService.getCustomers();
	}

	@GetMapping("/admin/customers/{customerId}")
	public CustomerResponse getCustomer(@PathVariable Long customerId) {
		GetCustomerByIdAndRoleServiceRequest requestPayload =
			new GetCustomerByIdAndRoleServiceRequest(
				customerId,
				UserRoleEnum.USER
			);

		return userService.getByUserIdAndRole(requestPayload);
	}

	@PostMapping("/admin/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public void createAdmin(@RequestBody UserCreateControllerRequest controllerRequest) {
		_createUserService(UserRoleEnum.ADMIN, controllerRequest);
	}

	private void _createUserService(UserRoleEnum role, UserCreateControllerRequest controllerRequest) {
		// 컨트롤러에서 전달받은 데이터를 기반으로 role을 할당하여 서비스에 전달한다.
		UserCreateServiceRequest serviceRequest =
			new UserCreateServiceRequest(
				role,
				controllerRequest.getName(),
				controllerRequest.getEmail(),
				controllerRequest.getUsername(),
				controllerRequest.getPassword(),
				controllerRequest.getNickname(),
				controllerRequest.getPhoneNumber()
			);

		userService.createUser(serviceRequest);
	}
}
