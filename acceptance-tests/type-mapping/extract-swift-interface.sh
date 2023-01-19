#!/bin/zsh

cd "build/test-temp/test-$1" || exit 1

# shellcheck disable=SC2028
echo "import Kotlin\n:type lookup Kotlin" | swift repl -F. > ../../generated-interface-"$1".swift

cd ../..

perl -i.original -p0e 's/  \@available\(swift, obsoleted: 3, renamed: ".*?\n//mg' generated-interface-"$1".swift
perl -i -p0e 's/  \@available\(\*, renamed: ".*?\n//mg' generated-interface-"$1".swift
perl -i -p0e 's/ \{\n    \@discardableResult \@objc get\n  \}//mg' generated-interface-"$1".swift
perl -i -p0e 's/ \{\n    @discardableResult @objc get\n\}//s' generated-interface-"$1".swift
