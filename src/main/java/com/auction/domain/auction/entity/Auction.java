package com.auction.domain.auction.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Entity
@NoArgsConstructor
public class Auction extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyerId")
    private User buyer;

    @Column(nullable = false)
    private int minPrice;
    private int maxPrice;

    private boolean isSold;
    private boolean isAutoExtension;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireAt;

    private Auction(Item item, User seller, int minPrice, boolean isAutoExtension, LocalTime expireAfter) {
        this.item = item;
        this.seller = seller;
        this.minPrice = minPrice;
        this.maxPrice = minPrice;
        this.isAutoExtension = isAutoExtension;
        this.expireAt = LocalDateTime.now().plusHours(expireAfter.getHour()).plusMinutes(expireAfter.getMinute());
    }
}
