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
        double p = 0;

        Date date = new Date();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        cal.setTime(date);

        double customerDiscount = getCustomerDiscount(b.getType());

        // Compute total amount depending on the types and quantity of product and
        // if we are in winter or summer discounts periods
        if (!isOnDiscountPeriods(cal)) {
            if (b.getItems() == null) {
                return "0";
            }

            for (int i = 0; i < b.getItems().length; i++) {
                Item it = b.getItems()[i];

                if (it.getType().equals("TSHIRT")) {
                    p += 30 * it.getNb() * customerDiscount;
                } else if (it.getType().equals("DRESS")) {
                    p += 50 * it.getNb() * customerDiscount;
                } else if (it.getType().equals("JACKET")) {
                    p += 100 * it.getNb() * customerDiscount;
                }
                // else if (it.getType().equals("SWEATSHIRT")) {
                //     price += 80 * it.getNb();
                // }
            }
        } else {
            if (b.getItems() == null) {
                return "0";
            }

            for (int i = 0; i < b.getItems().length; i++) {
                Item it = b.getItems()[i];

                if (it.getType().equals("TSHIRT")) {
                    p += 30 * it.getNb() * customerDiscount;
                } else if (it.getType().equals("DRESS")) {
                    p += 50 * it.getNb() * 0.8 * customerDiscount;
                } else if (it.getType().equals("JACKET")) {
                    p += 100 * it.getNb() * 0.9 * customerDiscount;
                }
                // else if (it.getType().equals("SWEATSHIRT")) {
                //     price += 80 * it.getNb();
                // }
            }
        }

        try {
            if (b.getType().equals("STANDARD_CUSTOMER")) {
                if (p > 200) {
                    throw new Exception("Price (" + p + ") is too high for standard customer");
                }
            } else if (b.getType().equals("PREMIUM_CUSTOMER")) {
                if (p > 800) {
                    throw new Exception("Price (" + p + ") is too high for premium customer");
                }
            } else if (b.getType().equals("PLATINUM_CUSTOMER")) {
                if (p > 2000) {
                    throw new Exception("Price (" + p + ") is too high for platinum customer");
                }
            } else {
                if (p > 200) {
                    throw new Exception("Price (" + p + ") is too high for standard customer");
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return String.valueOf(p);
    }

    private boolean isOnDiscountPeriods(Calendar cal) {
        for (DiscountPeriod p : DISCOUNT_PERIODS) {
            if (cal.get(Calendar.MONTH) == p.getMonth() && cal.get(Calendar.DAY_OF_MONTH) > p.getStartDay() && cal.get(Calendar.DAY_OF_MONTH) < p.getEndDay()) {
                return true;
            }
        }
        return false;
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
