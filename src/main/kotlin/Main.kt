import aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider
import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.model.DescribeAvailabilityZonesRequest
import aws.sdk.kotlin.services.ec2.model.DescribeInstancesRequest
import java.util.*

suspend fun listEc2Instances(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = DescribeInstancesRequest { }
        val response = ec2Client.describeInstances(request)

        println("EC2 Instances in region $region:")
        response.reservations?.forEach { reservation ->
            reservation.instances?.forEach { instance ->
                println(" - Instance ID: ${instance.instanceId}")
                println("   State: ${instance.state?.name}")
                println("   Type: ${instance.instanceType}")
                println("   Public DNS: ${instance.publicDnsName}")
                println("   Launch Time: ${instance.launchTime}")
                println()
            }
        }
    } catch (e: Exception) {
        println("Error fetching instances: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun listAvailableZones(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = DescribeAvailabilityZonesRequest { }
        val response = ec2Client.describeAvailabilityZones(request)

        println("Available Zones in region $region:")
        response.availabilityZones?.forEach { zone ->
            println(" - Zone Name: ${zone.zoneName}")
            println("   State: ${zone.state}")
            println("   Zone ID: ${zone.zoneId}")
            println("   Messages: ${zone.messages?.joinToString { it.message ?: "" }}")
            println()
        }
    } catch (e: Exception) {
        println("Error fetching availability zones: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun main() {
    val region = "ap-northeast-2" // 서울 리전

    val menu = Scanner(System.`in`)
    val idString = Scanner(System.`in`)

    var number = 0;
    while (true) {
        println("--------------------------------------------")
        println("            AWS  EC2 제어 프로그램            ")
        println("--------------------------------------------")
        println("1. 인스턴스 목록 조회")
        println("2. 가용 영역 조회")
        println("99. 종료")
        println("--------------------------------------------")

        print("입력하세요 : ")

        if(menu.hasNextInt()) {
            number = menu.nextInt();
        } else {
            println("메뉴에 없는 번호입니다.")
            break
        }

        when (number) {
            1 ->  {
                listEc2Instances(region)
            }
            2 -> {
                listAvailableZones(region)
            }
            99 -> return
        }
    }

}
