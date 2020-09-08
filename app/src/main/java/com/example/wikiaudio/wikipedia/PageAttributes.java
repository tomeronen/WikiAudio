package com.example.wikiaudio.wikipedia;

/**
 *      a list of page attributes you van ask to get on a page when querying.
 *     title - title of page.
 *     url - url of page.
 *     content - content of page.
 *     html - html of page.
 *     summary - summary of page.
 *     categories - categories which page belongs to.
 *     indicators - indicators of page.
 *     watchers - number of people which visited page.
 *     thumbnail - url to thumbnail image of page.
 *     longitude - longitude of page.
 *     latitude - latitude of page.
 *     audioUrl - url to audio file of page.
 */
public enum PageAttributes {
    title,
    url,
    content,
    description,
    categories,
    indicators,
    watchers,
    thumbnail,
    coordinates,
    audioUrl,
}
