/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.inspections

import org.rust.CheckTestmarkHit

class RsSortImplTraitMembersInspectionTest : RsInspectionsTestBase(RsSortImplTraitMembersInspection::class) {

    fun `test same order`() = checkFixIsUnavailable("Apply same member order", """
        struct Struct {
            i: i32
        }

        struct T;

        trait Trait {
            const ID1: i32;
            const ID2: i32;
            type T1;
            type T2;
            fn test1(&self) -> i32;
            fn test2(&self) -> i32;
            fn test3(&self) -> i32;
        }

        /*caret*/impl Trait for Struct {
            const ID1: i32 = 1;
            const ID2: i32 = 2;
            type T1 = T;
            type T2 = T;
            fn test1(&self) -> i32 {
                self.i
            }
            fn test2(&self) -> i32 {
                self.i * 2
            }
            fn test3(&self) -> i32 {
                self.i * 3
            }
        }
    """)

    fun `test empty`() = checkFixIsUnavailable("Apply same member order", """
        trait Foo {
        }

        /*caret*/impl Foo for () {
        }
    """)

    fun `test empty impl`() = checkFixIsUnavailable("Apply same member order", """
        trait Foo {
            type x;
        }

        /*caret*/impl Foo for () {
        }
    """)

    fun `test empty trait`() = checkFixIsUnavailable("Apply same member order", """
        trait Foo {
        }

        /*caret*/impl Foo for () {
            type x = ();
        }
    """)

    fun `test different impl`() = checkFixIsUnavailable("Apply same member order", """
        trait Foo {
            type x;
        }
        /*caret*/impl Foo for () {
            type y = ();
        }
    """)

    @CheckTestmarkHit(RsSortImplTraitMembersInspection.Testmarks.ImplMemberNotInTrait::class)
    fun `test impl with unknown members`() = checkFixIsUnavailable("Apply same member order", """
        trait Foo {
            fn f();
            fn g();
        }
        /*caret*/impl Foo for () {
            fn g() {}
            fn f() {}
            fn h() {}
        }
    """)

    fun `test different order`() = checkFixByText("Apply same member order", """
        struct Struct {
            i: i32
        }

        struct T;

        trait Trait {
            const ID1: i32;
            const ID2: i32;
            type T1;
            type T2;
            fn test1(&self) -> i32;
            fn test2(&self) -> i32;
            fn test3(&self) -> i32;
        }

        <weak_warning descr="Different impl member order from the trait">/*caret*/impl Trait for Struct</weak_warning> {
            type T2 = T;
            const ID2: i32 = 2;
            fn test3(&self) -> i32 {
                self.i * 3
            }
            fn test1(&self) -> i32 {
                self.i
            }
            fn test2(&self) -> i32 {
                self.i * 2
            }
            type T1 = T;
            const ID1: i32 = 1;
        }
    """, """
        struct Struct {
            i: i32
        }

        struct T;

        trait Trait {
            const ID1: i32;
            const ID2: i32;
            type T1;
            type T2;
            fn test1(&self) -> i32;
            fn test2(&self) -> i32;
            fn test3(&self) -> i32;
        }

        /*caret*/impl Trait for Struct {
            const ID1: i32 = 1;
            const ID2: i32 = 2;
            type T1 = T;
            type T2 = T;
            fn test1(&self) -> i32 {
                self.i
            }
            fn test2(&self) -> i32 {
                self.i * 2
            }
            fn test3(&self) -> i32 {
                self.i * 3
            }
        }
    """, checkWeakWarn = true)


    fun `test different order with same name`() = checkFixByText("Apply same member order", """
        trait Foo {
            type bar;
            fn bar();
        }

        <weak_warning descr="Different impl member order from the trait">/*caret*/impl Foo for ()</weak_warning> {
            fn bar() {
            }
            type bar = ();
        }
    """, """
        trait Foo {
            type bar;
            fn bar();
        }

        impl Foo for () {
            type bar = ();
            fn bar() {
            }
        }
    """, checkWeakWarn = true)

    fun `test highlight unsafe keyword`() = checkByText("""
        unsafe trait Foo {
            fn foo();
            fn bar();
        }

        <weak_warning descr="Different impl member order from the trait">/*caret*/unsafe impl Foo for ()</weak_warning> {
            fn bar() {
            }
            fn foo() {
            }
        }
    """, checkWeakWarn = true)

    fun `test highlight default keyword`() = checkByText("""
        unsafe trait Foo {
            fn foo();
            fn bar();
        }

        <weak_warning descr="Different impl member order from the trait">/*caret*/default unsafe impl Foo for ()</weak_warning> {
            fn bar() {
            }
            fn foo() {
            }
        }
    """, checkWeakWarn = true)

    fun `test do not highlight comments and attributes for impl`() = checkByText("""
        trait Foo {
            fn foo();
            fn bar();
        }

        // Some comment
        #[allow(something)]
        <weak_warning descr="Different impl member order from the trait">/*caret*/impl Foo for ()</weak_warning> {
            fn bar() {
            }
            fn foo() {
            }
        }
    """, checkWeakWarn = true)

    fun `test different order with different files`() = checkFixByFileTree("Apply same member order", """
        //- foo.rs
        pub trait Trait {
            const ID1: i32;
            const ID2: i32;
            type T1;
            type T2;
            fn test1(&self) -> i32;
            fn test2(&self) -> i32;
            fn test3(&self) -> i32;
        }

        //- main.rs
        mod foo;

        use foo::Trait;

        struct Struct {
            i: i32
        }

        struct T;

        <weak_warning descr="Different impl member order from the trait">/*caret*/impl Trait for Struct</weak_warning> {
            type T2 = T;
            const ID2: i32 = 2;
            fn test3(&self) -> i32 {
                self.i * 3
            }
            fn test1(&self) -> i32 {
                self.i
            }
            fn test2(&self) -> i32 {
                self.i * 2
            }
            type T1 = T;
            const ID1: i32 = 1;
        }
    """, """
        //- main.rs
        mod foo;

        use foo::Trait;

        struct Struct {
            i: i32
        }

        struct T;

        /*caret*/impl Trait for Struct {
            const ID1: i32 = 1;
            const ID2: i32 = 2;
            type T1 = T;
            type T2 = T;
            fn test1(&self) -> i32 {
                self.i
            }
            fn test2(&self) -> i32 {
                self.i * 2
            }
            fn test3(&self) -> i32 {
                self.i * 3
            }
        }
    """, checkWeakWarn = true)

    fun `test does the right thing when the impl is missing some of the members`() = checkFixByText("Apply same member order", """
        trait Foo {
            type T;
            const C: i32;
            fn foo();
        }

        struct S;

        <weak_warning descr="Different impl member order from the trait">impl Foo for S/*caret*/</weak_warning> {
            fn foo() { unimplemented!() }
            const C: i32 = unimplemented!();
        }
    """, """
        trait Foo {
            type T;
            const C: i32;
            fn foo();
        }

        struct S;

        impl Foo for S {
            const C: i32 = unimplemented!();
            fn foo() { unimplemented!() }
        }
    """, checkWeakWarn = true)
}
