package com.example.covidtracker.data

import com.google.gson.annotations.SerializedName

enum class State(val postalCode: String) {
    @SerializedName("AL")
    ALABAMA("AL"),

    @SerializedName("AK")
    ALASKA("AK"),

    @SerializedName("AZ")
    ARIZONA("AZ"),

    @SerializedName("AR")
    ARKANSAS("AR"),

    @SerializedName("CA")
    CALIFORNIA("CA"),

    @SerializedName("CO")
    COLORADO("CO"),

    @SerializedName("CT")
    CONNECTICUT("CT"),

    @SerializedName("DE")
    DELAWARE("DE"),

    @SerializedName("DC")
    DC("DC"),

    @SerializedName("FL")
    FLORIDA("FL"),

    @SerializedName("GA")
    GEORGIA("GA"),

    @SerializedName("HI")
    HAWAII("HI"),

    @SerializedName("ID")
    IDAHO("ID"),

    @SerializedName("IL")
    ILLINOIS("IL"),

    @SerializedName("IN")
    INDIANA("IN"),

    @SerializedName("IA")
    IOWA("IA"),

    @SerializedName("KS")
    KANSAS("KS"),

    @SerializedName("KY")
    KENTUCKY("KY"),

    @SerializedName("LA")
    LOUISIANA("LA"),

    @SerializedName("ME")
    MAINE("ME"),

    @SerializedName("MD")
    MARYLAND("MD"),

    @SerializedName("MA")
    MASSACHUSETTS("MA"),

    @SerializedName("MI")
    MICHIGAN("MI"),

    @SerializedName("MN")
    MINNESOTA("MN"),

    @SerializedName("MS")
    MISSISSIPPI("MS"),

    @SerializedName("MO")
    MISSOURI("MO"),

    @SerializedName("MT")
    MONTANA("MT"),

    @SerializedName("NE")
    NEBRASKA("NE"),

    @SerializedName("NV")
    NEVADA("NV"),

    @SerializedName("NH")
    NEW_HAMPSHIRE("NH"),

    @SerializedName("NJ")
    NEW_JERSEY("NJ"),

    @SerializedName("NM")
    NEW_MEXICO("NM"),

    @SerializedName("NY")
    NEW_YORK("NY"),

    @SerializedName("NC")
    NORTH_CAROLINA("NC"),

    @SerializedName("ND")
    NORTH_DAKOTA("ND"),

    @SerializedName("OH")
    OHIO("OH"),

    @SerializedName("OK")
    OKLAHOMA("OK"),

    @SerializedName("OR")
    OREGON("OR"),

    @SerializedName("PA")
    PENNSYLVANIA("PA"),

    @SerializedName("RI")
    RHODE_ISLAND("RI"),

    @SerializedName("SC")
    SOUTH_CAROLINA("SC"),

    @SerializedName("SD")
    SOUTH_DAKOTA("SD"),

    @SerializedName("TN")
    TENNESSEE("TN"),

    @SerializedName("TX")
    TEXAS("TX"),

    @SerializedName("UT")
    UTAH("UT"),

    @SerializedName("VT")
    VERMONT("VT"),

    @SerializedName("VA")
    VIRGINIA("VA"),

    @SerializedName("WA")
    WASHINGTON("WA"),

    @SerializedName("WV")
    WEST_VIRGINIA("WV"),

    @SerializedName("WI")
    WISCONSIN("WI"),

    @SerializedName("WY")
    WYOMING("WY")



}