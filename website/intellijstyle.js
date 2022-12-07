var colors = {
    function: "#ffc66d",
    comment: "#808080",
    char: "#e8bf6a",
    keyword: "#cc7832",
    boolean: "#cc7832",
    primitive: "#6897bb",
    string: "#6a8759",
    variable: "#a9b7c6",
    variable2: "#9876aa",
    className: "#a9b7c6",
    method: "#ffc66d",

    //Don't know what these should be...
    punctuation: "#c8d0de",
    tag: "#ff0000",
    operator: "#ff0000"
};

const theme = {
    plain: {
        backgroundColor: "#2b2b2b",
        color: "#c8d0de"
    },
    styles: [{
        types: ["attr-name"],
        style: {
            color: colors.keyword
        }
    }, {
        types: ["attr-value"],
        style: {
            color: colors.string
        }
    }, {
        types: ["comment", "block-comment", "prolog", "doctype", "cdata", "shebang"],
        style: {
            color: colors.comment
        }
    }, {
        types: ["property", "number", "function-name", "constant", "symbol", "deleted"],
        style: {
            color: colors.primitive
        }
    }, {
        types: ["boolean"],
        style: {
            color: colors.boolean
        }
    }, {
        types: ["tag"],
        style: {
            color: colors.tag
        }
    }, {
        types: ["string"],
        style: {
            color: colors.string
        }
    }, {
        types: ["punctuation"],
        style: {
            color: colors.punctuation
        }
    }, {
        types: ["selector", "char", "builtin", "inserted"],
        style: {
            color: colors.char
        }
    }, {
        types: ["function"],
        style: {
            color: colors.function
        }
    }, {
        types: ["operator", "entity", "url"],
        style: {
            color: colors.variable
        }
    }, {
        types: ["variable", "property", "constant", "delimiter"],
        style: {
            color: colors.variable2
        }
    }, {
        types: ["keyword"],
        style: {
            color: colors.keyword
        }
    }, {
        types: ["at-rule", "class-name"],
        style: {
            color: colors.className
        }
    }, {
        types: ["important"],
        style: {
            fontWeight: "400"
        }
    }, {
        types: ["bold"],
        style: {
            fontWeight: "bold"
        }
    }, {
        types: ["italic"],
        style: {
            fontStyle: "italic"
        }
    }, {
        types: ["namespace"],
        style: {
            opacity: 0.7
        }
    }]
};

// export default theme;

module.exports = theme;