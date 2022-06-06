package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exeption.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;

@SpringBootTest
@Transactional
class OrderServiceTest {

	@Autowired
	EntityManager em;
	@Autowired
	OrderService orderService;
	@Autowired
	OrderRepository orderRepository;

	@Test
	void 상품주문() {
		// given
		Member member = createMember();
		Book book = createBook("JPA", 10000, 10);

		int orderCount = 2;

		// when
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

		// then
		Order getOrder = orderRepository.findOne(orderId);
		assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
		assertThat(getOrder.getOrderItems().size()).isEqualTo(1);
		assertThat(getOrder.getTotalPrice()).isEqualTo(10000*orderCount);
		assertThat(book.getStockQuantity()).isEqualTo(8);
	}

	@Test
	void 주문취소() {
		// given
		Member member = createMember();
		Book book = createBook("JPA", 10000, 10);

		int orderCount = 2;
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
		// when
		orderService.cancelOrder(orderId);
		// then
		Order getOrder = orderRepository.findOne(orderId);
		assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
		assertThat(book.getStockQuantity()).isEqualTo(10);
	}

	@Test
	void 상품주문_재고수량초과() {
		// given
		Member member = createMember();
		Book book = createBook("JPA", 10000, 10);

		int orderCount = 11;
		// when

		// then
		assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount)).isInstanceOf(NotEnoughStockException.class);
	}

	private Book createBook(String name, int price, int stockQuantity) {
		Book book = new Book();
		book.setName(name);
		book.setPrice(price);
		book.setStockQuantity(stockQuantity);
		em.persist(book);
		return book;
	}

	private Member createMember() {
		Member member = new Member();
		member.setName("회원1");
		member.setAddress(new Address("서울", "강가", "123123"));
		em.persist(member);
		return member;
	}
}
