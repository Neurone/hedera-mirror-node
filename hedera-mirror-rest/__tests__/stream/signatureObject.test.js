/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2022 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

'use strict';

import {INT_SIZE, LONG_SIZE} from '../../stream/constants';
import SignatureObject from '../../stream/signatureObject';

const {SHA_384_WITH_RSA} = SignatureObject;

describe('SignatureObject', () => {
  let buffer;
  const classId = BigInt.asIntN(64, BigInt('0xf422da83a251741e'));
  const classVersion = 1;
  const signature = new Array(SHA_384_WITH_RSA.maxLength).fill(0xab);

  beforeEach(() => {
    buffer = Buffer.from(
      [].concat(
        [0xf4, 0x22, 0xda, 0x83, 0xa2, 0x51, 0x74, 0x1e], // classId
        [0, 0, 0, 1], // classVersion
        [0, 0, 0, 1], // signature type, SHA384WithRSA
        [0, 0, 0x1, 0x80], // length, 384
        [0xff, 0xff, 0xfe, 0xe5], // checksum
        signature
      )
    );
  });

  it('getLength', () => {
    const expected = buffer.length;
    const signatureObject = new SignatureObject(buffer);
    expect(signatureObject.getLength()).toEqual(expected);
  });

  it('classId', () => {
    const signatureObject = new SignatureObject(buffer);
    expect(signatureObject.classId).toEqual(classId);
  });

  it('classVersion', () => {
    const signatureObject = new SignatureObject(buffer);
    expect(signatureObject.classVersion).toEqual(classVersion);
  });

  it('signatureType', () => {
    const signatureObject = new SignatureObject(buffer);
    expect(signatureObject.type).toEqual(SHA_384_WITH_RSA.type);
  });

  it('signature', () => {
    const signatureObject = new SignatureObject(buffer);
    expect(signatureObject.signature).toEqual(Buffer.from(signature));
  });

  it('checksum mismatch', () => {
    buffer[LONG_SIZE + INT_SIZE + INT_SIZE] = 1;
    expect(() => new SignatureObject(buffer.slice(0, buffer.length - 4))).toThrowErrorMatchingSnapshot();
  });

  it('truncated buffer', () => {
    expect(() => new SignatureObject(buffer.slice(0, buffer.length - 4))).toThrowErrorMatchingSnapshot();
  });

  it('unknown signatureType', () => {
    buffer[LONG_SIZE + INT_SIZE] = 1;
    expect(() => new SignatureObject(buffer)).toThrowErrorMatchingSnapshot();
  });
});
