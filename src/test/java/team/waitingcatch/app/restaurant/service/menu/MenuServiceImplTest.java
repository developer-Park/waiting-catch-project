package team.waitingcatch.app.restaurant.service.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import team.waitingcatch.app.common.util.S3Uploader;
import team.waitingcatch.app.restaurant.dto.menu.CreateMenuServiceRequest;
import team.waitingcatch.app.restaurant.dto.menu.MenuResponse;
import team.waitingcatch.app.restaurant.dto.menu.UpdateMenuServiceRequest;
import team.waitingcatch.app.restaurant.entity.Menu;
import team.waitingcatch.app.restaurant.entity.Restaurant;
import team.waitingcatch.app.restaurant.repository.MenuRepository;
import team.waitingcatch.app.restaurant.service.restaurant.InternalRestaurantService;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private InternalRestaurantService restaurantService;

	@Mock
	private S3Uploader s3Uploader;

	@InjectMocks
	private MenuServiceImpl menuService;

	@Nested
	@DisplayName("메뉴 생성")
	class CreateMenu {

		@Test
		@DisplayName("이미지가 없는 경우")
		void createMenuWithoutImage() {
			// given
			CreateMenuServiceRequest serviceRequest = mock(CreateMenuServiceRequest.class);
			Restaurant restaurant = mock(Restaurant.class);
			MultipartFile multipartFile = mock(MultipartFile.class);

			when(serviceRequest.getMultipartFile()).thenReturn(multipartFile);
			when(serviceRequest.getMultipartFile().isEmpty()).thenReturn(true);
			when(restaurantService._getRestaurant(any(Long.class))).thenReturn(restaurant);

			// when
			menuService.createMenu(serviceRequest);

			// then
			verify(menuRepository, times(1)).save(any(Menu.class));
		}

		@Test
		@DisplayName("이미지가 있는 경우")
		void createMenuWithImage() throws IOException {
			// given
			CreateMenuServiceRequest serviceRequest = mock(CreateMenuServiceRequest.class);
			Restaurant restaurant = mock(Restaurant.class);
			MultipartFile multipartFile = mock(MultipartFile.class);

			when(serviceRequest.getMultipartFile()).thenReturn(multipartFile);
			when(serviceRequest.getMultipartFile().isEmpty()).thenReturn(false);
			when(restaurantService._getRestaurant(any(Long.class))).thenReturn(restaurant);

			// when
			menuService.createMenu(serviceRequest);

			// then
			verify(menuRepository, times(1)).save(any(Menu.class));
		}
	}

	@Test
	@DisplayName("자신의 레스토랑 메뉴 조회")
	void getMyRestaurantMenus() {
		// given
		List<Menu> menus = new ArrayList<>();
		Menu menu = mock(Menu.class);
		menus.add(menu);

		when(menu.getName()).thenReturn("aaa");
		when(menuRepository.findAllByRestaurantId(any(Long.class))).thenReturn(menus);

		// when
		List<MenuResponse> responses = menuService.getMyRestaurantMenus(any(Long.class));

		// then
		assertEquals("aaa", responses.get(0).getName());
	}

	@Test
	@DisplayName("_getMenuByRestaurantId 메소드")
	void _getMenuByRestaurantId() {
		// given
		List<Menu> menus = new ArrayList<>();
		Menu menu = mock(Menu.class);
		menus.add(menu);

		when(menu.getName()).thenReturn("aaa");
		when(menuRepository.findAllByRestaurantId(any(Long.class))).thenReturn(menus);

		// when
		List<Menu> menus1 = menuService._getMenusByRestaurantId(any(Long.class));

		// then
		assertEquals("aaa", menus1.get(0).getName());
	}

	@Nested
	@DisplayName("메뉴 수정")
	class updateMenu {

		@Test
		@DisplayName("수정할 이미지가 없는 경우")
		void updateMenuWithoutImage() {
			// given
			UpdateMenuServiceRequest serviceRequest = mock(UpdateMenuServiceRequest.class);
			Restaurant restaurant = mock(Restaurant.class);
			Menu menu = new Menu(restaurant, "aaa", 100, "image");
			MultipartFile multipartFile = mock(MultipartFile.class);

			when(serviceRequest.getName()).thenReturn("bbb");
			when(serviceRequest.getMultipartFile()).thenReturn(multipartFile);
			when(serviceRequest.getMultipartFile().isEmpty()).thenReturn(true);
			when(menuRepository.findById(any(Long.class))).thenReturn(Optional.of(menu));

			// when
			menuService.updateMenu(serviceRequest);

			// then
			assertEquals("bbb", menu.getName());
		}

		@Test
		@DisplayName("수정할 이미지가 있는 경우")
		void updateMenuWithImage() {
			// given
			UpdateMenuServiceRequest serviceRequest = mock(UpdateMenuServiceRequest.class);
			Restaurant restaurant = mock(Restaurant.class);
			Menu menu = new Menu(restaurant, "aaa", 100, "image");
			MultipartFile multipartFile = mock(MultipartFile.class);

			when(serviceRequest.getName()).thenReturn("bbb");
			when(serviceRequest.getMultipartFile()).thenReturn(multipartFile);
			when(serviceRequest.getMultipartFile().isEmpty()).thenReturn(false);
			when(menuRepository.findById(any(Long.class))).thenReturn(Optional.of(menu));

			// when
			menuService.updateMenu(serviceRequest);

			// then
			assertEquals("bbb", menu.getName());
		}
	}
}