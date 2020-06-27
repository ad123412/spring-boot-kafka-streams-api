package com.ada.kafka.streams.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Transaction {
    private int id;
    private String name;
    private String type;
    private int amount;
    private long timeInMillisecond;
}
