package caffeinateme.steps;

import caffeinateme.model.*;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Steps;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderCoffeeSteps {
    @Steps(shared = true)
    Customer cathy = new Customer("Cathy");
    ProductCatalog productCatalog = new ProductCatalog();
    CoffeeShop coffeeShop = new CoffeeShop(productCatalog);
    Order order;
    Receipt receipt;

    @Given("Cathy is {int} metre(s) from the coffee shop")
    public void cathy_is_metres_from_the_coffee_shop(Integer distanceInMetres) {
        cathy.setDistanceFromShop(distanceInMetres);
    }

    @When("^Cathy (?:orders|has ordered) an? (.*)")
    public void cathy_orders_a(String orderedProduct) {
        this.order = Order.of(1,orderedProduct).forCustomer(cathy);
        cathy.placesAnOrderFor(order).at(coffeeShop);
    }

    @Then("Barry should receive the order")
    public void barry_should_receive_the_order() {
        assertThat(coffeeShop.getPendingOrders()).contains(order);
    }

    @ParameterType(name = "order-status", value="(Normal|High|Urgent)")
    public OrderStatus orderStatus(String statusValue) {
        return OrderStatus.valueOf(statusValue);
    }

    @Then("Barry should know that the order is {order-status}")
    public void barry_should_know_that_the_order_is(OrderStatus expectedStatus) {
        Order cathysOrder = coffeeShop.getOrderFor(cathy)
                .orElseThrow(() -> new AssertionError("No order found!"));
        assertThat(cathysOrder.getStatus()).isEqualTo(expectedStatus);
    }

    @And("Cathy is {int} minutes away")
    public void customerIsMinutesAway(int etaInMinutes) {
        coffeeShop.setCustomerETA(cathy, etaInMinutes);
    }

    @DataTableType
    public ProductPrice mapRowToProductPrice(Map<String, String> row) {
        System.out.println(row.get("Product"));
        return new ProductPrice(row.get("Product"), Double.parseDouble(row.get("Price")));
    }

    @Given("^the following prices:$")
    public void theFollowingPrices(List<ProductPrice> productPrices) {
        productCatalog.addProductsWithPrices(productPrices);

    }

    @DataTableType
    public OrderItem mapRowToOrderItem(Map<String, String> row) {
        return new OrderItem(row.get("Product"), Integer.parseInt(row.get("Quantity")));
    }

    @Given("Cathy has ordered:")
    public void cathyHasOrdered(List<OrderItem> orders) {
        for(OrderItem item : orders) {
            Order order = Order.of(item.getQuantity(),item.getProduct()).forCustomer(cathy);
            cathy.placesAnOrderFor(order).at(coffeeShop);
        }
    }

    @When("she asks for a receipt")
    public void sheAsksForAReceipt() {
        receipt = coffeeShop.getReceiptFor(cathy);
    }

    @Then("she should receive a receipt totalling:")
    public void sheShouldReceiveAReceiptTotalling(List<Map<String, String>> receiptTotals) {
        Double serviceFee = Double.parseDouble(receiptTotals.get(0).get("Service Fee"));
        Double subTotal = Double.parseDouble(receiptTotals.get(0).get("Subtotal"));
        Double total = Double.parseDouble(receiptTotals.get(0).get("Total"));

        assertThat(receipt.getServiceFee()).isEqualTo(serviceFee);
        assertThat(receipt.getSubtotal()).isEqualTo(subTotal);
        assertThat(receipt.getTotal()).isEqualTo(total);
    }

    @DataTableType
    public ReceiptLineItem mapRowToReceiptLineItems(Map<String, String> row){
        return new ReceiptLineItem(row.get("Product"), Integer.parseInt(row.get("Quantity")),
                Double.parseDouble(row.get("Price")));
    }

    @And("the receipt should contain the line items:")
    public void theReceiptShouldContainTheLineItems(List<ReceiptLineItem> expectedReceiptLineItems) {
        assertThat(receipt.getLineItems()).containsExactlyElementsOf(expectedReceiptLineItems);
    }

}