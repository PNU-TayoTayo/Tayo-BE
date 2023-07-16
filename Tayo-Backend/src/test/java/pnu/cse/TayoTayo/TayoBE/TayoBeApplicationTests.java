package pnu.cse.TayoTayo.TayoBE;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class TayoBeApplicationTests {

	@Test
	void contextLoads() {

		String s = LocalDate.now().minusYears(15).toString();
		System.out.println(s);

		String s1 = "2022-07-16";


	}

}
