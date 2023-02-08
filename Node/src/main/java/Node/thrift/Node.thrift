/*
 * This is a simplified version of the tutorial made available at
 * thrift.apache.org.
 * Changes made by <ronald.moore@h-da.de>.
 *
 * Original Thrift Tutorial
 * by  Mark Slee (mcslee@facebook.com)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


/**
 * The available types in Thrift are:
 *
 *  bool        Boolean, one byte
 *  i8 (byte)   Signed 8-bit integer
 *  i16         Signed 16-bit integer
 *  i32         Signed 32-bit integer
 *  i64         Signed 64-bit integer
 *  double      64-bit floating point value
 *  string      String
 *  binary      Blob (byte array)
 *  map<t1,t2>  Map from one type to another
 *  list<t1>    Ordered list of one type
 *  set<t1>     Set of unique elements of one type
 *
 */

/**
 * Thrift files can namespace, package, or prefix their output in various
 * target languages.
 */

// namespace cpp simpleTutorial
// namespace java Node.thrift
// namespace php simpleTutorial
// namespace perl simpleTutorial

/**
 * You can define enums, which are just 32 bit integers. Values are optional
 * and start at 1 if not supplied, C style again.
 */


/**
 * Structs are the basic complex data structures. They are comprised of fields
 * which each have an integer identifier, a type, a symbolic name, and an
 * optional default value.
 *
 * Fields can be declared "optional", which ensures they will not be included
 * in the serialized output if they aren't set.  Note that this requires some
 * manual management in some languages.
 */


/**
 * Structs can also be exceptions, if they are nasty.
 */
/**
* exception InvalidOperation {
*  1: string why
*}
*/

/**
 * Ahh, now onto the cool part, defining a service. Services just need a name.
 */
service NodeConfig{

   bool setMax(1: i32 newValue),

   bool setMin(1: i32 newValue),

   bool turnOff(),

   bool turnOn()

}
