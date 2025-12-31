import { Module, Global } from '@nestjs/common';
import { SendPulseService } from './sendpulse.service';

@Global()
@Module({
  providers: [SendPulseService],
  exports: [SendPulseService],
})
export class SendPulseModule {}
