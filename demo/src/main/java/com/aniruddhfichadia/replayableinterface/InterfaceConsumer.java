package com.aniruddhfichadia.replayableinterface;


/**
 * @author Aniruddh Fichadia | Email: Ani.Fichadia@gmail.com | GitHub Username: AniFichadia
 *         (http://github.com/AniFichadia)
 * @date 2017-08-20
 */
public class InterfaceConsumer {
    private Demo.NestedInterface replayableNestedInterface = new ReplayableDemo$NestedInterface();
    private Demo.NestedInterface realNestedInterface       = new RealNestedInterface();


    public static void main(String[] args) {
        InterfaceConsumer interfaceConsumer = new InterfaceConsumer();

        interfaceConsumer.testFunctionality();


//        Output:

//        Delegate is not bound
//        Invoking superMethod()
//        Invoking methodWithoutSideEffect()
//        Replaying against real implementation
//        superMethod() called
//        Binding real implementation
//        Invoking setMessage(String)
//        setMessage() called with: text = [some message]
    }


    public void testFunctionality() {
        System.out.println("Delegate is not bound");

        System.out.println("Invoking superMethod()");
        replayableNestedInterface.superMethod();
        System.out.println("Invoking methodWithoutSideEffect()");
        replayableNestedInterface.methodWithoutSideEffect();


        System.out.println("Replaying against real implementation");
        ((ReplaySource<Demo.NestedInterface>) replayableNestedInterface).replay(
                realNestedInterface
        );

        System.out.println("Binding real implementation");
        ((Delegator<Demo.NestedInterface>) replayableNestedInterface).bindDelegate(
                realNestedInterface
        );

        System.out.println("Invoking setMessage(String)");
        realNestedInterface.setMessage("some message");
    }


    class RealNestedInterface
            implements Demo.NestedInterface {
        @Override
        public void superMethod() {
            System.out.println("superMethod() called");
        }

        @Override
        public void methodWithoutSideEffect() {
            System.out.println("methodWithoutSideEffect() called");
        }

        @Override
        public void methodWithParamUniqueCalls(String aParam, boolean anotherParam) {
            System.out.println(
                    "methodWithParamUniqueCalls() called with: aParam = [" + aParam + "], anotherParam = [" + anotherParam + "]");
        }

        @Override
        public void somethingEnqueueable(String aParam) {
            System.out.println("somethingEnqueueable() called with: aParam = [" + aParam + "]");
        }

        @Override
        public void showLoading() {
            System.out.println("showLoading() called");
        }

        @Override
        public void hideLoading() {
            System.out.println("hideLoading() called");
        }

        @Override
        public void setMessage(String text) {
            System.out.println("setMessage() called with: text = [" + text + "]");
        }

        @Override
        public void setLoadingAllowed(boolean allowed) {
            System.out.println("setLoadingAllowed() called with: allowed = [" + allowed + "]");
        }

        @Override
        public int returnsSomething() {
            System.out.println("returnsSomething() called");
            return 0;
        }
    }
}
