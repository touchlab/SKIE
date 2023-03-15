---
title: JSON Configuration File Example
---

:::caution
We don't recommend using JSON configuration files. We recommend using the Gradle DSL or annotations instead.
:::

SKIE can also be configured using JSON files which can then be imported in Gradle. An example of such file is shown below:

```json title=skie.json
{
    "enabledFeatures": {
        "features": [
            "CoroutinesInterop"
        ]
    },
    "groups": [
        {
            "target": "",
            "overridesAnnotations": false,
            "items": {
                "ExperimentalFeatures.Enabled": "true"
            }
        }
    ]
}
```

To learn more about what's possible, look at the `config.json` files in `acceptance-tests/src/test/resources`.
