package com.auction.domain.auction.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RefundLog extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    private int price;
    private boolean isConsumed;

    private RefundLog(User user, Auction auction, int price, boolean isConsumed) {
        this.user = user;
        this.auction = auction;
        this.price = price;
        this.isConsumed = isConsumed;
    }
    public static RefundLog of(User user, Auction auction, int price, boolean isConsumed) {
        return new RefundLog(user, auction, price, isConsumed);
    }
    public void consumed() {this.isConsumed = true;}
}
