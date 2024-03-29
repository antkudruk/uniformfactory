package com.github.antkudruk.example.gameexample.domain;

import com.github.antkudruk.example.gameexample.gameengine.Color;
import com.github.antkudruk.example.gameexample.gameobject.Identity;
import com.github.antkudruk.example.gameexample.gameobject.Property;
import com.github.antkudruk.example.gameexample.gameobject.JmeObject;
import com.github.antkudruk.example.gameexample.gameengine.Bone;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Human {

    @Identity
    private final String name = "Mike";

    @JmeObject(nodeName = "rightArm")
    private Bone rightArm;
    @JmeObject(nodeName = "leftArm")
    private Bone leftArm;

    @Property(name = "pantsColor")
    private Color pantsColor;

    @Property(name = "age")
    private int age;

    @Property(name = "salary")
    private BigDecimal salary;

    private Bone head;

    @JmeObject(nodeName = "head")
    public void setHead(Bone head) {
        this.head = head;
    }
}
