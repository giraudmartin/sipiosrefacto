package com.sipios.refactoring.controller;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/shopping")
public class ShoppingController {

    private Logger logger = LoggerFactory.getLogger(ShoppingController.class);

    private static final List<DiscountPeriod> DISCOUNT_PERIODS = new ArrayList<>() {{
        add(new DiscountPeriod("Summer period", Calendar.JUNE, 5, 15));
        add(new DiscountPeriod("Winter period", Calendar.JANUARY, 5, 15));
    }};


    @PostMapping
    public String getPrice(@RequestBody Body b) {

        double price = 0;

        Customer customer = Customer.getInstance(b.getType());
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // If shopping cart is empty return 0
        if (b.getItems() == null) {
            return "0";
        } else {

            Date date = new Date();
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
            cal.setTime(date);

            boolean isOnDiscountPeriods = isOnDiscountPeriods(cal);

            // Compute total amount depending on the types and quantity of product and
            // if we are in winter or summer discounts periods
            for (Item it : b.getItems()) {
                switch(it.getType()) {
                    case "TSHIRT":
                        price += 30 * it.getNb() * customer.getDiscount();
                        break;
                    case "DRESS":
                        price += 50 * it.getNb() * (isOnDiscountPeriods ? 0.8 : 1) * customer.getDiscount();
                        break;
                    case "JACKET":
                        price += 100 * it.getNb() * (isOnDiscountPeriods ? 0.9 : 1) * customer.getDiscount();
                        break;
                }
            }
        }

        try {
            if (price > customer.getMaxPrice()) {
                throw new Exception("Price (" + price + ") is too high for " + customer.getLabel() + " customer");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return String.valueOf(price);
    }

    private boolean isOnDiscountPeriods(Calendar cal) {
        for (DiscountPeriod p : DISCOUNT_PERIODS) {
            if (cal.get(Calendar.MONTH) == p.getMonth() && cal.get(Calendar.DAY_OF_MONTH) > p.getStartDay() && cal.get(Calendar.DAY_OF_MONTH) < p.getEndDay()) {
                return true;
            }
        }
        return false;
    }

    private enum Customer
    {
        STANDARD_CUSTOMER("standard", 1, 200),
        PREMIUM_CUSTOMER("prenium",0.9, 800),
        PLATINUM_CUSTOMER("platinium",0.5, 2000);

        private final String label;
        private final double discount;
        private final double maxPrice;

        Customer(String label, double discount, double maxPrice) {
            this.label = label;
            this.discount = discount;
            this.maxPrice = maxPrice;
        }

        public static Customer getInstance(String customerType) {
            switch(customerType) {
                case "STANDARD_CUSTOMER":
                    return STANDARD_CUSTOMER;
                case "PREMIUM_CUSTOMER":
                    return PREMIUM_CUSTOMER;
                case "PLATINUM_CUSTOMER":
                    return PLATINUM_CUSTOMER;
                default:
                    return null;
            }
        }

        public String getLabel() {
            return label;
        }

        public double getDiscount() {
            return discount;
        }

        public double getMaxPrice() {
            return maxPrice;
        }
    }

    private double getCustomerDiscount(String customerType) {
        switch(customerType) {
            case "STANDARD_CUSTOMER":
                return 1;
            case "PREMIUM_CUSTOMER":
                return 0.9;
            case "PLATINUM_CUSTOMER":
                return 0.5;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

}

class Body {

    private Item[] items;
    private String type;

    public Body(Item[] is, String t) {
        this.items = is;
        this.type = t;
    }

    public Body() {}

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

class DiscountPeriod {

    private String title;
    private int month;
    private int startDay;
    private int endDay;

    public DiscountPeriod(String title, int month, int startDay, int endDay) {
        this.title = title;
        this.month = month;
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }
}

class Item {

    private String type;
    private int nb;

    public Item() {}

    public Item(String type, int quantity) {
        this.type = type;
        this.nb = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNb() {
        return nb;
    }

    public void setNb(int nb) {
        this.nb = nb;
    }
}
