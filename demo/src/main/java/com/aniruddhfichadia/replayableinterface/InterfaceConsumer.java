package com.aniruddhfichadia.replayableinterface;


/**
 * @author Aniruddh Fichadia | Email: Ani.Fichadia@gmail.com | GitHub Username: AniFichadia
 * (http://github.com/AniFichadia)
 * @date 2017-08-20
 */
public class InterfaceConsumer {
    public void init() {
        DemoInterface.NestedInterface nestedInterface = new ReplayableDemoInterface$NestedInterface();

        InterfaceWithGenerics<String> interfaceWithGenerics = new ReplayableInterfaceWithGenerics<>();
    }

}
