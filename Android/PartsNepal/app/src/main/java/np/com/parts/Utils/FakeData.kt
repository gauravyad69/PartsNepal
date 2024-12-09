package np.com.parts.Utils

import com.github.javafaker.Faker
import np.com.parts.API.Models.KhaltiAmountBreakdown
import np.com.parts.API.Models.KhaltiCustomerInfo
import np.com.parts.API.Models.KhaltiPaymentRequest
import np.com.parts.API.Models.KhaltiProductDetail

fun generateFakeData(): KhaltiPaymentRequest {
    val faker = Faker()

    val customerInfo = KhaltiCustomerInfo(
        name = faker.name().fullName(),
        email = "gauravyad2077@gmail.com",
        phone = "9746621180"
    )

    val amountBreakdown = listOf(
        KhaltiAmountBreakdown(
            label = "Mark Price",
            amount = faker.number().numberBetween(500, 2000)
        ),
        KhaltiAmountBreakdown(
            label = "VAT",
            amount = faker.number().numberBetween(100, 500)
        )
    )

    val productDetails = listOf(
        KhaltiProductDetail(
            identity = faker.idNumber().valid(),
            name = faker.commerce().productName(),
            total_price = 1300,
            quantity = faker.number().numberBetween(1, 5),
            unit_price = 1300
        )
    )

    return KhaltiPaymentRequest(
        return_url = faker.internet().url(),
        website_url = faker.internet().url(),
        amount = 1300,
        purchase_order_id = faker.commerce().promotionCode(),
        purchase_order_name = faker.commerce().productName(),
        customer_info = customerInfo,
        amount_breakdown = amountBreakdown,
        product_details = productDetails,
        merchant_username = faker.company().name(),
        merchant_extra = faker.company().industry()
    )
}
