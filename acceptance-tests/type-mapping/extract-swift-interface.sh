#!/bin/zsh
cd build/test-temp || exit 1

# shellcheck disable=SC2028
echo "import Kotlin\n:type lookup Kotlin" | swift repl -F. > ../generated-interface.swift

cd ..

perl -i.original -p0e 's/  \@available\(swift, obsoleted: 3, renamed: ".*?\n//mg' generated-interface.swift
perl -i -p0e 's/  \@available\(\*, renamed: ".*?\n//mg' generated-interface.swift
perl -i -p0e 's/ \{\n    \@discardableResult \@objc get\n  \}//mg' generated-interface.swift
perl -i -p0e 's/ \{\n    @discardableResult @objc get\n\}//s' generated-interface.swift
