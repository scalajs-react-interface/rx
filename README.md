# Rx

[Rx](https://github.com/lihaoyi/scala.rx) and React match made in heaven!.


#### Experimental


1) To use Var[T] as props  use `RxVarComponent`

2) To use Rx[T] as props use `RxComponent`

3) RxComponent/RxVarComponent skip updates coming from parent , they only updated when `Var/Rx` changed.

4) if you're overriding `componentDidMount` or `componentWillUnMount` then make sure you call super calls.
