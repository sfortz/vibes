<?xml version="1.0" encoding="UTF-8"?>
<fts>
    <start>state0</start>
    <states>
        <state id="state0">
            <transition action="map" target="state1" fexpression="mapping"></transition>
            <transition action="liDet" target="state2" fexpression="(!mapping &amp;&amp; lidar)"></transition>
            <transition action="caDet" target="state2" fexpression="(!mapping &amp;&amp; camera)"></transition>
            <transition action="move" target="state3" fexpression="!mapping"></transition>
        </state>
        <state id="state5">
            <transition action="charge" target="state4" fexpression="true"></transition>
        </state>
        <state id="state2">
            <transition action="goAround" target="state3" fexpression="true"></transition>
        </state>
        <state id="state1">
            <transition action="liDet" target="state2" fexpression="(mapping &amp;&amp; lidar)"></transition>
            <transition action="caDet" target="state2" fexpression="(mapping &amp;&amp; camera)"></transition>
            <transition action="move" target="state3" fexpression="mapping"></transition>
        </state>
        <state id="state4"></state>
        <state id="state3">
            <transition action="clean" target="state5" fexpression="true"></transition>
        </state>
    </states>
</fts>