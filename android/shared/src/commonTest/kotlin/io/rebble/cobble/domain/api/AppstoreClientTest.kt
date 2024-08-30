package io.rebble.cobble.domain.api

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.rebble.cobble.shared.api.AppstoreClient
import io.rebble.libpebblecommon.util.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AppstoreClientTest {
    @Test
    fun `Get locker data`() = runBlocking {
        val data = """
            {
                "applications": [
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "605fc3730889610007b67707",
                            "name": "Kiwiscripter"
                        },
                        "hardware_platforms": [
                            {
                                "description": "This is a watchface heavily inspired by the original macintosh personal computer.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/656538bce46ef1000900e6b9",
                                    "screenshot": "https://assets2.rebble.io/144x168/656538bae46ef1000900e6b6"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 129,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "This is a watchface heavily inspired by the original macintosh personal computer.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/180x180/656538bce46ef1000900e6b9",
                                    "screenshot": "https://assets2.rebble.io/180x180/656538bce46ef1000900e6b7"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 193,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "This is a watchface heavily inspired by the original macintosh personal computer.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/656538bce46ef1000900e6b9",
                                    "screenshot": "https://assets2.rebble.io/144x168/656538bce46ef1000900e6b8"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 257,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 49,
                        "id": "656538bae46ef1000900e6b5",
                        "is_configurable": false,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/c51d938b-2a15-4340-94b5-4f4fcb0d6474",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/c51d938b-2a15-4340-94b5-4f4fcb0d6474",
                            "share": "https://apps.rebble.io/applications/656538bae46ef1000900e6b5"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/656538bce46ef1000900e6ba.pbw",
                            "icon_resource_id": 0,
                            "release_id": "656538bce46ef1000900e6ba"
                        },
                        "title": "Vintage Computer",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "c51d938b-2a15-4340-94b5-4f4fcb0d6474",
                        "version": "1.0"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "5b3b1836712710000164e3ca",
                            "name": "The Resetter"
                        },
                        "hardware_platforms": [
                            {
                                "description": "Is the watch drunk?",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/6497ceac741dd4050959a9fa",
                                    "screenshot": "https://assets2.rebble.io/144x168/6497ceac741dd4050959a9f7"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 65,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "Is the watch drunk?",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/6497ceac741dd4050959a9fa",
                                    "screenshot": "https://assets2.rebble.io/144x168/6497ceac741dd4050959a9f7"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 129,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Is the watch drunk?",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/6497ceac741dd4050959a9fa",
                                    "screenshot": "https://assets2.rebble.io/144x168/6497ceac741dd4050959a9f7"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 257,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 8,
                        "id": "6497ceac741dd4050959a9f6",
                        "is_configurable": false,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/4a334bf9-ed72-456f-b76b-b460403ca14f",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/4a334bf9-ed72-456f-b76b-b460403ca14f",
                            "share": "https://apps.rebble.io/applications/6497ceac741dd4050959a9f6"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/6497d977741dd4050959a9fc.pbw",
                            "icon_resource_id": 3,
                            "release_id": "6497d977741dd4050959a9fc"
                        },
                        "title": "Woozy",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "4a334bf9-ed72-456f-b76b-b460403ca14f",
                        "version": "1.29"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "63121c3a5e45c40008d0bc52",
                            "name": "nyrikiri"
                        },
                        "hardware_platforms": [
                            {
                                "description": "A watchface inspired by a game made by Videocult called \"Rain World\"\r\nFeatures:\r\n- a scug\r\n- cycle going through the day and refilling at night\r\n- karma level as current battery level\r\n- a scenery from one of 7 different ingame regions for every weekday (only on color screens!)",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/65e4dfe746ea9f022d8bf763",
                                    "screenshot": "https://assets2.rebble.io/144x168/65e4dfe646ea9f022d8bf75f"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 65,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "A watchface inspired by a game made by Videocult called \"Rain World\"\r\nFeatures:\r\n- a scug\r\n- cycle going through the day and refilling at night\r\n- karma level as current battery level\r\n- a scenery from one of 7 different ingame regions for every weekday (only on color screens!)",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/65e4dfe746ea9f022d8bf763",
                                    "screenshot": "https://assets2.rebble.io/144x168/65e4dfe746ea9f022d8bf760"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 129,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 31,
                        "id": "65e4dfe646ea9f022d8bf75e",
                        "is_configurable": false,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/4d01e1bb-4db4-4f91-b456-3cb9362fe209",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/4d01e1bb-4db4-4f91-b456-3cb9362fe209",
                            "share": "https://apps.rebble.io/applications/65e4dfe646ea9f022d8bf75e"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/65e4dfe746ea9f022d8bf764.pbw",
                            "icon_resource_id": 1,
                            "release_id": "65e4dfe746ea9f022d8bf764"
                        },
                        "title": "Rain World",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "4d01e1bb-4db4-4f91-b456-3cb9362fe209",
                        "version": "1.0"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "5dcf62c7c393f5706985a9cb",
                            "name": "lavendork"
                        },
                        "hardware_platforms": [
                            {
                                "description": "The ultimate Ben 10 watch face for Pebble! A successor to my first Pebble watch face, with better looks and more features.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/6617645f46ea9f09c2b15595",
                                    "screenshot": "https://assets2.rebble.io/144x168/6617645c46ea9f09c2b1558d"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 73,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "The ultimate Ben 10 watch face for Pebble! A successor to my first Pebble watch face, with better looks and more features.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/6617645f46ea9f09c2b15595",
                                    "screenshot": "https://assets2.rebble.io/144x168/6617645e46ea9f09c2b1558f"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 137,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "The ultimate Ben 10 watch face for Pebble! A successor to my first Pebble watch face, with better looks and more features.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/180x180/6617645f46ea9f09c2b15595",
                                    "screenshot": "https://assets2.rebble.io/180x180/6617645e46ea9f09c2b15591"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 201,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "The ultimate Ben 10 watch face for Pebble! A successor to my first Pebble watch face, with better looks and more features.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/6617645f46ea9f09c2b15595",
                                    "screenshot": "https://assets2.rebble.io/144x168/6617645f46ea9f09c2b15593"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 265,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 11,
                        "id": "6617645c46ea9f09c2b1558c",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/8b227bd2-e5dc-40d1-93b8-aeecdccfa51b",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/8b227bd2-e5dc-40d1-93b8-aeecdccfa51b",
                            "share": "https://apps.rebble.io/applications/6617645c46ea9f09c2b1558c"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/6617645f46ea9f09c2b15596.pbw",
                            "icon_resource_id": 1,
                            "release_id": "6617645f46ea9f09c2b15596"
                        },
                        "title": "Minitrix2",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "8b227bd2-e5dc-40d1-93b8-aeecdccfa51b",
                        "version": "0.1"
                    },
                    {
                        "category": "Remotes",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "529f67f51a663894e5000071",
                            "name": "metroidmen"
                        },
                        "hardware_platforms": [
                            {
                                "description": "The first true Philips Hue light controlling app for Pebble! Control the power, the color, and brightness straight from your wrist! Options for Philips presets are present, along with other fun extras included as well!\r\n\r\nFor any issues, try entering your IP address manually in the settings page!",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/RZD6OKHsSdyFzHo0hRTR",
                                    "list": "https://assets2.rebble.io/exact/144x144/BcTJe68eSo2ukP9OLLZC",
                                    "screenshot": "https://assets2.rebble.io/144x168/p38WGuJQT5ONndM03Gvu"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 8,
                                "sdk_version": "5.13"
                            }
                        ],
                        "hearts": 885,
                        "id": "529f6dbdd7894bb069000056",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/667f6000-1b0e-49ce-8260-66f1043d0b08",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/667f6000-1b0e-49ce-8260-66f1043d0b08",
                            "share": "https://apps.rebble.io/applications/529f6dbdd7894bb069000056"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/5438cd34cf07ca59d2000143.pbw",
                            "icon_resource_id": 1,
                            "release_id": "5438cd34cf07ca59d2000143"
                        },
                        "title": "Huebble",
                        "type": "watchapp",
                        "user_token": "",
                        "uuid": "667f6000-1b0e-49ce-8260-66f1043d0b08",
                        "version": "1.29"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "609801a58324660009e8a4db",
                            "name": "John Spahr"
                        },
                        "hardware_platforms": [
                            {
                                "description": "A simple and elegant analog watchface with optional date and plenty of other customization options. Built with RockyJS.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/62964f21b4b75900091ae62e",
                                    "screenshot": "https://assets2.rebble.io/144x168/62964f20b4b75900091ae62b"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 169,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "A simple and elegant analog watchface with optional date and plenty of other customization options. Built with RockyJS.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/180x180/62964f21b4b75900091ae62e",
                                    "screenshot": "https://assets2.rebble.io/180x180/62964f20b4b75900091ae62d"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 233,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "A simple and elegant analog watchface with optional date and plenty of other customization options. Built with RockyJS.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/62964f21b4b75900091ae62e",
                                    "screenshot": "https://assets2.rebble.io/144x168/62964f20b4b75900091ae62b"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 297,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 10,
                        "id": "62964f20b4b75900091ae62a",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/dab37791-dab7-dab1-8d2f-e73f52743dab",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/dab37791-dab7-dab1-8d2f-e73f52743dab",
                            "share": "https://apps.rebble.io/applications/62964f20b4b75900091ae62a"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/62a66f31b4b75900091ae634.pbw",
                            "icon_resource_id": 0,
                            "release_id": "62a66f31b4b75900091ae634"
                        },
                        "title": "Cupertino",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "dab37791-dab7-dab1-8d2f-e73f52743dab",
                        "version": "1.2"
                    },
                    {
                        "category": "Tools & Utilities",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "559ca4038f524fbd020000ac",
                            "name": "Johannes Neubrand"
                        },
                        "hardware_platforms": [
                            {
                                "description": "Pomodoro made simple: Solanum.\r\n\r\nSolanum is a simple, but powerful pomodoro-like timer for Pebble. It lets you know when to start and stop working, and uses an uncluttered, simple design to show you what's important at a glance and helping you manage your time.\r\n\r\nTags: Time management, time-management, pomodoro, solanum, timer",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/7Oe9ilOVS5uUpXzatQk7",
                                    "list": "https://assets2.rebble.io/exact/144x144/j5czyddzQlCRWkPfg5Dc",
                                    "screenshot": "https://assets2.rebble.io/200x228/a1YPoB0ISCed2o2hvnju"
                                },
                                "name": "emery",
                                "pebble_process_info_flags": 328,
                                "sdk_version": "5.84"
                            },
                            {
                                "description": "Pomodoro made simple: Solanum.\r\n\r\nSolanum is a simple, but powerful pomodoro-like timer for Pebble. It lets you know when to start and stop working, and uses an uncluttered, simple design to show you what's important at a glance and helping you manage your time.\r\n\r\nTags: Time management, time-management, pomodoro, solanum, timer",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/7Oe9ilOVS5uUpXzatQk7",
                                    "list": "https://assets2.rebble.io/exact/144x144/j5czyddzQlCRWkPfg5Dc",
                                    "screenshot": "https://assets2.rebble.io/144x168/Q2ksS3lTwaUBEGmowwIE"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 72,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "Pomodoro made simple: Solanum.\r\n\r\nSolanum is a simple, but powerful pomodoro-like timer for Pebble. It lets you know when to start and stop working, and uses an uncluttered, simple design to show you what's important at a glance and helping you manage your time.\r\n\r\nTags: Time management, time-management, pomodoro, solanum, timer",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/7Oe9ilOVS5uUpXzatQk7",
                                    "list": "https://assets2.rebble.io/exact/144x144/j5czyddzQlCRWkPfg5Dc",
                                    "screenshot": "https://assets2.rebble.io/144x168/6Qyqa3CcQ9esR4C71xYO"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 136,
                                "sdk_version": "5.84"
                            },
                            {
                                "description": "Pomodoro made simple: Solanum.\r\n\r\nSolanum is a simple, but powerful pomodoro-like timer for Pebble. It lets you know when to start and stop working, and uses an uncluttered, simple design to show you what's important at a glance and helping you manage your time.\r\n\r\nTags: Time management, time-management, pomodoro, solanum, timer",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/7Oe9ilOVS5uUpXzatQk7",
                                    "list": "https://assets2.rebble.io/exact/144x144/j5czyddzQlCRWkPfg5Dc",
                                    "screenshot": "https://assets2.rebble.io/180x180/u4W3F7lPTlmaBbSRgZon"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 200,
                                "sdk_version": "5.84"
                            },
                            {
                                "description": "Pomodoro made simple: Solanum.\r\n\r\nSolanum is a simple, but powerful pomodoro-like timer for Pebble. It lets you know when to start and stop working, and uses an uncluttered, simple design to show you what's important at a glance and helping you manage your time.\r\n\r\nTags: Time management, time-management, pomodoro, solanum, timer",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/7Oe9ilOVS5uUpXzatQk7",
                                    "list": "https://assets2.rebble.io/exact/144x144/j5czyddzQlCRWkPfg5Dc",
                                    "screenshot": "https://assets2.rebble.io/144x168/Q2HF9TiQWKvQpY2q5pZ4"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 264,
                                "sdk_version": "5.84"
                            }
                        ],
                        "hearts": 487,
                        "id": "5639b1a377b75e5dfd000030",
                        "is_configurable": true,
                        "is_timeline_enabled": true,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/7d4bba82-35cb-48c6-a30b-bdc7422fba28",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/7d4bba82-35cb-48c6-a30b-bdc7422fba28",
                            "share": "https://apps.rebble.io/applications/5639b1a377b75e5dfd000030"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/581ffb1ce7785b8942000033.pbw",
                            "icon_resource_id": 2,
                            "release_id": "581ffb1ce7785b8942000033"
                        },
                        "title": "Solanum",
                        "type": "watchapp",
                        "user_token": "",
                        "uuid": "7d4bba82-35cb-48c6-a30b-bdc7422fba28",
                        "version": "3.3"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "5e28ea923dd3109f151c7e08",
                            "name": "Harrison Allen"
                        },
                        "hardware_platforms": [
                            {
                                "description": "Pebblemon Time!\n\nExplore the Pok\u00e9mon world of Pebblemon, but with a twist: the watchface does all the exploring for you! Every minute, your character will take another step forward through the world, no input required.\n\nTry giving your watch a shake, and see what happens to your character!\n\nThis is just the overworld portion of Pebblemon, no battles included.\n\nFeatures a full day/morning/night cycle!\n\nSupports all Pebbles!\n\n-------\nThis is a watchface demo using the same graphics library as Pebblemon. I was able to reduce the size of the code and resources enough for this watchface to fit on the Pebble Classic and Steel!\n",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/60eff8f21342ae009770ca47",
                                    "screenshot": "https://assets2.rebble.io/144x168/60eff8e81342ae009770ca33"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 73,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "Pebblemon Time!\n\nExplore the Pok\u00e9mon world of Pebblemon, but with a twist: the watchface does all the exploring for you! Every minute, your character will take another step forward through the world, no input required.\n\nTry giving your watch a shake, and see what happens to your character!\n\nThis is just the overworld portion of Pebblemon, no battles included.\n\nFeatures a full day/morning/night cycle!\n\nSupports all Pebbles!\n\n-------\nThis is a watchface demo using the same graphics library as Pebblemon. I was able to reduce the size of the code and resources enough for this watchface to fit on the Pebble Classic and Steel!\n",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/60eff8f21342ae009770ca47",
                                    "screenshot": "https://assets2.rebble.io/144x168/60eff8eb1342ae009770ca38"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 137,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Pebblemon Time!\n\nExplore the Pok\u00e9mon world of Pebblemon, but with a twist: the watchface does all the exploring for you! Every minute, your character will take another step forward through the world, no input required.\n\nTry giving your watch a shake, and see what happens to your character!\n\nThis is just the overworld portion of Pebblemon, no battles included.\n\nFeatures a full day/morning/night cycle!\n\nSupports all Pebbles!\n\n-------\nThis is a watchface demo using the same graphics library as Pebblemon. I was able to reduce the size of the code and resources enough for this watchface to fit on the Pebble Classic and Steel!\n",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/180x180/60eff8f21342ae009770ca47",
                                    "screenshot": "https://assets2.rebble.io/180x180/60eff8ed1342ae009770ca3d"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 201,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Pebblemon Time!\n\nExplore the Pok\u00e9mon world of Pebblemon, but with a twist: the watchface does all the exploring for you! Every minute, your character will take another step forward through the world, no input required.\n\nTry giving your watch a shake, and see what happens to your character!\n\nThis is just the overworld portion of Pebblemon, no battles included.\n\nFeatures a full day/morning/night cycle!\n\nSupports all Pebbles!\n\n-------\nThis is a watchface demo using the same graphics library as Pebblemon. I was able to reduce the size of the code and resources enough for this watchface to fit on the Pebble Classic and Steel!\n",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/60eff8f21342ae009770ca47",
                                    "screenshot": "https://assets2.rebble.io/144x168/60eff8f01342ae009770ca42"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 265,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 99,
                        "id": "60eff8e81342ae009770ca32",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/2b5ff5d9-75a2-4f8e-866d-015216a3e45c",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/2b5ff5d9-75a2-4f8e-866d-015216a3e45c",
                            "share": "https://apps.rebble.io/applications/60eff8e81342ae009770ca32"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/610028601342ae007b5f0288.pbw",
                            "icon_resource_id": 1,
                            "release_id": "610028601342ae007b5f0288"
                        },
                        "title": "Pebblemon Time",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "2b5ff5d9-75a2-4f8e-866d-015216a3e45c",
                        "version": "2.0"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "53846b55dc3ec44e97000058",
                            "name": "dP-watchfaces"
                        },
                        "hardware_platforms": [
                            {
                                "description": "A short time ago in a galaxy near, near away... there was the only one really smartwatch: Pebble.\r\n\r\nGalasix is a galaxy style watchface which you can get six data information: time, date, weather temperature, step count, battery charge, step goal progress.\r\n\r\n+ Battery constellation, 2 groups of 5 stars.\r\n+ Step goal progress meteor.\r\n\r\nCUSTOM YOUR GALASIX GALAXY: color presets or custom colors, show/hide: battery constellation, step goal meteor, background stars.\r\n\r\n(Time, Weather, Health)\r\n\r\nDesigned and developed by dP-watchfaces\r\nCheck out http://dp-watchfaces.com/\r\n\r\n---\r\n${'$'}1.01 via KiezelPay at \r\nwww.kzl.io/cwp\r\n\r\n(24h Trial)\r\n",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/XgHl1SC0Qu2w1fkcayVV",
                                    "screenshot": "https://assets2.rebble.io/144x168/XgHl1SC0Qu2w1fkcayVV"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 137,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "A short time ago in a galaxy near, near away... there was the only one really smartwatch: Pebble.\r\n\r\nGalasix is a galaxy style watchface which you can get six data information: time, date, weather temperature, step count, battery charge, step goal progress.\r\n\r\n+ Battery constellation, 2 groups of 5 stars.\r\n+ Step goal progress meteor.\r\n\r\nCUSTOM YOUR GALASIX GALAXY: color presets or custom colors, show/hide: battery constellation, step goal meteor, background stars.\r\n\r\n(Time, Weather, Health)\r\n\r\nDesigned and developed by dP-watchfaces\r\nCheck out http://dp-watchfaces.com/\r\n\r\n---\r\n${'$'}1.01 via KiezelPay at \r\nwww.kzl.io/cwp\r\n\r\n(24h Trial)\r\n",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/180x180/XgHl1SC0Qu2w1fkcayVV",
                                    "screenshot": "https://assets2.rebble.io/180x180/oi0y0tSCQOGH4oD6Snbn"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 201,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "A short time ago in a galaxy near, near away... there was the only one really smartwatch: Pebble.\r\n\r\nGalasix is a galaxy style watchface which you can get six data information: time, date, weather temperature, step count, battery charge, step goal progress.\r\n\r\n+ Battery constellation, 2 groups of 5 stars.\r\n+ Step goal progress meteor.\r\n\r\nCUSTOM YOUR GALASIX GALAXY: color presets or custom colors, show/hide: battery constellation, step goal meteor, background stars.\r\n\r\n(Time, Weather, Health)\r\n\r\nDesigned and developed by dP-watchfaces\r\nCheck out http://dp-watchfaces.com/\r\n\r\n---\r\n${'$'}1.01 via KiezelPay at \r\nwww.kzl.io/cwp\r\n\r\n(24h Trial)\r\n",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/XgHl1SC0Qu2w1fkcayVV",
                                    "screenshot": "https://assets2.rebble.io/144x168/uMv5KYBQQ5SJk7QlZeh8"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 265,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 225,
                        "id": "586387f00e3a494b82000236",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/39a0e964-0292-4d3b-9e81-2023a3ae3c76",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/39a0e964-0292-4d3b-9e81-2023a3ae3c76",
                            "share": "https://apps.rebble.io/applications/586387f00e3a494b82000236"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/5863f2dd5de8509ab500029d.pbw",
                            "icon_resource_id": 3,
                            "release_id": "5863f2dd5de8509ab500029d"
                        },
                        "title": "Galasix",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "39a0e964-0292-4d3b-9e81-2023a3ae3c76",
                        "version": "1.1"
                    },
                    {
                        "category": "Tools & Utilities",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": false
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "608059d81342ae003ce73f00",
                            "name": "JSystems"
                        },
                        "hardware_platforms": [
                            {
                                "description": "Pebble Deliveries is an app that makes it easy to track your shipment from dozens of different postal carriers. Simply app your package name and tracking number from the settings page to begin tracking.\n\nIf you have any issues or suggestions, you can file them through the Github issues page by clicking \"Website Link\" at the bottom of the screen.\n\nDonations (All donations are appreciated!):\nBTC: bc1q3gemldvwpy8mz7t8em9xljkf6c0r2kszl7g28w\nBCH: bitcoincash:qznzhqx30gzza6wz60lxecj6hax4x0ay2uktwp8hdw\nETH: 0x947AcB586FF42d56cFBea2EC73F51a438Dd3773a\n",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/60a4dcbe1342ae010c229b5f",
                                    "list": "https://assets2.rebble.io/exact/144x144/60a4dcbc1342ae010c229b5e",
                                    "screenshot": "https://assets2.rebble.io/144x168/60a4dc991342ae010c229b4a"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 72,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "Pebble Deliveries is an app that makes it easy to track your shipment from dozens of different postal carriers. Simply app your package name and tracking number from the settings page to begin tracking.\n\nIf you have any issues or suggestions, you can file them through the Github issues page by clicking \"Website Link\" at the bottom of the screen.\n\nDonations (All donations are appreciated!):\nBTC: bc1q3gemldvwpy8mz7t8em9xljkf6c0r2kszl7g28w\nBCH: bitcoincash:qznzhqx30gzza6wz60lxecj6hax4x0ay2uktwp8hdw\nETH: 0x947AcB586FF42d56cFBea2EC73F51a438Dd3773a\n",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/60a4dcbe1342ae010c229b5f",
                                    "list": "https://assets2.rebble.io/exact/144x144/60a4dcbc1342ae010c229b5e",
                                    "screenshot": "https://assets2.rebble.io/144x168/60a4dca11342ae010c229b4f"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 136,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Pebble Deliveries is an app that makes it easy to track your shipment from dozens of different postal carriers. Simply app your package name and tracking number from the settings page to begin tracking.\n\nIf you have any issues or suggestions, you can file them through the Github issues page by clicking \"Website Link\" at the bottom of the screen.\n\nDonations (All donations are appreciated!):\nBTC: bc1q3gemldvwpy8mz7t8em9xljkf6c0r2kszl7g28w\nBCH: bitcoincash:qznzhqx30gzza6wz60lxecj6hax4x0ay2uktwp8hdw\nETH: 0x947AcB586FF42d56cFBea2EC73F51a438Dd3773a\n",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/60a4dcbe1342ae010c229b5f",
                                    "list": "https://assets2.rebble.io/exact/144x144/60a4dcbc1342ae010c229b5e",
                                    "screenshot": "https://assets2.rebble.io/180x180/60a4dcaa1342ae010c229b54"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 200,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Pebble Deliveries is an app that makes it easy to track your shipment from dozens of different postal carriers. Simply app your package name and tracking number from the settings page to begin tracking.\n\nIf you have any issues or suggestions, you can file them through the Github issues page by clicking \"Website Link\" at the bottom of the screen.\n\nDonations (All donations are appreciated!):\nBTC: bc1q3gemldvwpy8mz7t8em9xljkf6c0r2kszl7g28w\nBCH: bitcoincash:qznzhqx30gzza6wz60lxecj6hax4x0ay2uktwp8hdw\nETH: 0x947AcB586FF42d56cFBea2EC73F51a438Dd3773a\n",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/60a4dcbe1342ae010c229b5f",
                                    "list": "https://assets2.rebble.io/exact/144x144/60a4dcbc1342ae010c229b5e",
                                    "screenshot": "https://assets2.rebble.io/144x168/60a4dcb21342ae010c229b59"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 264,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 55,
                        "id": "608059da1342ae003ce73f02",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/9b6e24a7-d369-4568-90b0-82a95df875d8",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/9b6e24a7-d369-4568-90b0-82a95df875d8",
                            "share": "https://apps.rebble.io/applications/608059da1342ae003ce73f02"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/60a4dcc01342ae010c229b60.pbw",
                            "icon_resource_id": 1,
                            "release_id": "60a4dcc01342ae010c229b60"
                        },
                        "title": "Pebble Deliveries",
                        "type": "watchapp",
                        "user_token": "",
                        "uuid": "9b6e24a7-d369-4568-90b0-82a95df875d8",
                        "version": "1.3"
                    },
                    {
                        "category": "Games",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "52ab9742951e34594b000002",
                            "name": "StuartHa"
                        },
                        "hardware_platforms": [
                            {
                                "description": "Port of Flappy Bird. By Stuart Harrell.\r\n\r\nSupport developer by donating via Paypal to skh076000@gmail.com\r\n\r\nColorized for Pebble Time by Frank Martinez (Dezign999).",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/jd9acWmwSywm9Q87w82w",
                                    "list": "https://assets2.rebble.io/exact/144x144/mhF0tWsOR8SYIYyd4bL4",
                                    "screenshot": "https://assets2.rebble.io/144x168/M0o8DSBTvyUdv3Ga7v3h"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 0,
                                "sdk_version": "5.19"
                            },
                            {
                                "description": "Clone of Flappy Bird by Stuart Harrell.\r\n\r\nSupport developer by donating via Paypal to skh076000@gmail.com\r\n\r\nColorized by Frank Martinez (Dezign999).",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/jd9acWmwSywm9Q87w82w",
                                    "list": "https://assets2.rebble.io/exact/144x144/mhF0tWsOR8SYIYyd4bL4",
                                    "screenshot": "https://assets2.rebble.io/144x168/di7wjxMRRyOd7YtliWwN"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 0,
                                "sdk_version": "5.72"
                            },
                            {
                                "description": "Clone of Flappy Bird by Stuart Harrell. Support developer by donating via Paypal to skh076000@gmail.com. Colorized and roundified by Frank Martinez (Dezign999).",
                                "images": {
                                    "icon": "https://assets2.rebble.io/exact/48x48/jd9acWmwSywm9Q87w82w",
                                    "list": "https://assets2.rebble.io/exact/144x144/mhF0tWsOR8SYIYyd4bL4",
                                    "screenshot": "https://assets2.rebble.io/180x180/1xCXMgSRK69MwBlCYRe1"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 0,
                                "sdk_version": "5.72"
                            }
                        ],
                        "hearts": 3701,
                        "id": "52f7a04b777f7eee6d000258",
                        "is_configurable": false,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/2edff9df-4d60-47db-9962-437d40c5f1e2",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/2edff9df-4d60-47db-9962-437d40c5f1e2",
                            "share": "https://apps.rebble.io/applications/52f7a04b777f7eee6d000258"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/566cf8c9d129bdd44f0000a0.pbw",
                            "icon_resource_id": 1,
                            "release_id": "566cf8c9d129bdd44f0000a0"
                        },
                        "title": "Tiny Bird",
                        "type": "watchapp",
                        "user_token": "",
                        "uuid": "2edff9df-4d60-47db-9962-437d40c5f1e2",
                        "version": "2.0"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "541d0dc59a11beda980000c8",
                            "name": "Alexey 'Cluster' Avdyukhin"
                        },
                        "hardware_platforms": [
                            {
                                "description": "Super Mario jumps every minute. It can show weather, watch battery level and vibrate when disconnects from phone. You can also download Android companion app to show phone battery level. And there is color version for Pebble Time too.\r\nBased on a concept by Denis Dzyubenko.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/epP1ziuaRXKm7xqXuFxi",
                                    "screenshot": "https://assets2.rebble.io/144x168/4NBmJat6RPqZc6f8Azx7"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 73,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "Super Mario jumps every minute. It's color version for Pebble Time. Background rotates during day or you can just select one on your own. It can show weather, watch battery level and vibrate when disconnects from phone. You can also download Android companion app to show phone battery level. Based on a concept by Denis Dzyubenko.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/epP1ziuaRXKm7xqXuFxi",
                                    "screenshot": "https://assets2.rebble.io/144x168/epP1ziuaRXKm7xqXuFxi"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 137,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Super Mario jumps every minute. It's color version for Pebble Time. Background rotates during day or you can just select one on your own. It can show weather, watch battery level and vibrate when disconnects from phone. You can also download Android companion app to show phone battery level. Based on a concept by Denis Dzyubenko.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/180x180/epP1ziuaRXKm7xqXuFxi",
                                    "screenshot": "https://assets2.rebble.io/180x180/HYmaybPQQCk2Udue4trY"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 201,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Super Mario jumps every minute. It can show weather, watch battery level and vibrate when disconnects from phone. You can also download Android companion app to show phone battery level. And there is color version for Pebble Time too.\r\nBased on a concept by Denis Dzyubenko.",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/epP1ziuaRXKm7xqXuFxi",
                                    "screenshot": "https://assets2.rebble.io/144x168/jkXsXasxRRuJj5ThRfKu"
                                },
                                "name": "diorite",
                                "pebble_process_info_flags": 265,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 7357,
                        "id": "55431083b7d4a71c0000003b",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/43caa750-2896-4f46-94dc-1adbd4bc1ff3",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/43caa750-2896-4f46-94dc-1adbd4bc1ff3",
                            "share": "https://apps.rebble.io/applications/55431083b7d4a71c0000003b"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/6293d137b4b75900091ae627.pbw",
                            "icon_resource_id": 14,
                            "release_id": "6293d137b4b75900091ae627"
                        },
                        "title": "Mario Time Watchface",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "43caa750-2896-4f46-94dc-1adbd4bc1ff3",
                        "version": "3.41"
                    },
                    {
                        "category": "Faces",
                        "companions": {
                            "android": null,
                            "ios": null
                        },
                        "compatibility": {
                            "android": {
                                "supported": true
                            },
                            "aplite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "basalt": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "chalk": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "diorite": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "emery": {
                                "firmware": {
                                    "major": 3
                                },
                                "supported": true
                            },
                            "ios": {
                                "min_js_version": 1,
                                "supported": true
                            }
                        },
                        "developer": {
                            "contact_email": "noreply@rebble.io",
                            "id": "52f013b5077b1db7990000c0",
                            "name": "reno"
                        },
                        "hardware_platforms": [
                            {
                                "description": "Welcome to Weather Land - watch the landscape change with your local weather conditions! \r\n\r\n\u2022 14+ weather designs\r\n\u2022 Battery meter\r\n\u2022 30 min refresh\r\n\u2022 Bluetooth Disconnection Alert\r\n\r\nEnjoy Weather Land? Love it with a heart or email comments/feedback. :)",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/Boz3krFRTjKqNp3oMpd4",
                                    "screenshot": "https://assets2.rebble.io/144x168/7ZQwPhFzRnOon0lC154m"
                                },
                                "name": "aplite",
                                "pebble_process_info_flags": 73,
                                "sdk_version": "5.78"
                            },
                            {
                                "description": "Welcome to Weather Land - watch the landscape change with your local weather conditions! \r\n\r\n\u2022 14+ weather designs\r\n\u2022 Battery meter\r\n\u2022 30 min refresh\r\n\u2022 Bluetooth Disconnection Alert\r\n\r\nEnjoy Weather Land? Love it with a heart or email comments/feedback. :)",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/144x168/Boz3krFRTjKqNp3oMpd4",
                                    "screenshot": "https://assets2.rebble.io/144x168/Boz3krFRTjKqNp3oMpd4"
                                },
                                "name": "basalt",
                                "pebble_process_info_flags": 137,
                                "sdk_version": "5.86"
                            },
                            {
                                "description": "Welcome to Weather Land - watch the landscape change with your local weather conditions! \r\n\r\n\u2022 14+ weather designs\r\n\u2022 Battery meter\r\n\u2022 30 min refresh\r\n\u2022 Bluetooth Disconnection Alert\r\n\r\nEnjoy Weather Land? Love it with a heart or email comments/feedback. :)",
                                "images": {
                                    "icon": "",
                                    "list": "https://assets2.rebble.io/exact/180x180/Boz3krFRTjKqNp3oMpd4",
                                    "screenshot": "https://assets2.rebble.io/180x180/eC7iX3tcRGkmio074iBw"
                                },
                                "name": "chalk",
                                "pebble_process_info_flags": 201,
                                "sdk_version": "5.86"
                            }
                        ],
                        "hearts": 10448,
                        "id": "53381b17d1719b42b800028b",
                        "is_configurable": true,
                        "is_timeline_enabled": false,
                        "links": {
                            "href": "https://appstore-api.rebble.io/api/v1/locker/1f0b0701-cc8f-47ec-86e7-7181397f9a25",
                            "remove": "https://appstore-api.rebble.io/api/v1/locker/1f0b0701-cc8f-47ec-86e7-7181397f9a25",
                            "share": "https://apps.rebble.io/applications/53381b17d1719b42b800028b"
                        },
                        "pbw": {
                            "file": "https://storage.googleapis.com/rebble-pbws/pbw/6089dd611342ae6f4dcc98fe.pbw",
                            "icon_resource_id": 1,
                            "release_id": "6089dd611342ae6f4dcc98fe"
                        },
                        "title": "Weather Land",
                        "type": "watchface",
                        "user_token": "",
                        "uuid": "1f0b0701-cc8f-47ec-86e7-7181397f9a25",
                        "version": "4.0"
                    }
                ]
            }
        """.trimIndent()
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/locker" -> {
                    if (request.headers[HttpHeaders.Authorization] != "Bearer x") {
                        respond(
                                "Unauthorized",
                                status = HttpStatusCode.Unauthorized
                        )
                    }
                    val response = when (request.method) {
                        HttpMethod.Get -> {
                            respond(
                                    data,
                                    headers = headersOf(
                                            HttpHeaders.ContentType, "application/json"
                                    )
                            )
                        }
                        else -> error("Unsupported method")
                    }
                    response
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        val client = AppstoreClient("https://appstore-api.rebble.io/api", "x", mockEngine)
        val locker = client.getLocker()
        assertEquals(13, locker.size)
    }
}